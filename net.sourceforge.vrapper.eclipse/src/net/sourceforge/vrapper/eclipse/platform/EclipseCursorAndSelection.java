package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.interceptor.EditorInfo;
import net.sourceforge.vrapper.eclipse.ui.CaretUtils;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.InsertMode;

public class EclipseCursorAndSelection implements CursorService, SelectionService {

    public static final String POSITION_CATEGORY_NAME = "net.sourceforge.vrapper.position";
    /// Marker type for global (A-Z0-9) marks. Use IMarker.MARKER to make them invisible.
    public static final String GLOBAL_MARK_TYPE = IMarker.BOOKMARK;
    private final EditorInfo editorInfo;
    private final ITextViewer textViewer;
    private int stickyColumn;
    private boolean stickToEOL = false;
    private final ITextViewerExtension5 converter;
    private Selection selection;
    private boolean selectionInProgress;
    private final SelectionChangeListener selectionChangeListener;
    private final StickyColumnUpdater caretListener;
    private final Map<String, org.eclipse.jface.text.Position> marks;
    private final List<org.eclipse.jface.text.Position> changeList;
    private int changeListIndex;
    private final Configuration configuration;
    private final EclipseTextContent textContent;
    private int averageCharWidth;
    private CaretType caretType = null;
    private Point caretCachedSize;
    private VisualCaretPainter visualCaretPainter;
    private VrapperModeRecorder vrapperModeRecorder;

    public EclipseCursorAndSelection(VrapperModeRecorder vrapperModeRecorder, final Configuration configuration,
            EditorInfo editorInfo, final ITextViewer textViewer, final EclipseTextContent textContent) {
        this.vrapperModeRecorder = vrapperModeRecorder;
        this.configuration = configuration;
        this.editorInfo = editorInfo;
        this.textViewer = textViewer;
        this.textContent = textContent;
        StyledText tw = textViewer.getTextWidget();
        this.stickyColumn = tw.getLeftMargin();
        GC gc = null;
        try {
            gc = new GC(tw);
            averageCharWidth = (int)Math.round(gc.getFontMetrics().getAverageCharacterWidth());
        } finally {
            if (gc != null)
                gc.dispose();
        }
        converter = OffsetConverter.create(textViewer);
        selectionChangeListener = new SelectionChangeListener();
        caretListener = new StickyColumnUpdater();
        marks = new HashMap<String, org.eclipse.jface.text.Position>();
        changeList = new ArrayList<org.eclipse.jface.text.Position>();
        visualCaretPainter = new VisualCaretPainter();
    }

    public void installHooks() {
        textViewer.getTextWidget().addSelectionListener(selectionChangeListener);
        textViewer.getTextWidget().addCaretListener(caretListener);
        textViewer.getTextWidget().addPaintListener(visualCaretPainter);
        textViewer.getSelectionProvider().addSelectionChangedListener(selectionChangeListener);
        textViewer.getDocument().addPositionCategory(POSITION_CATEGORY_NAME);
    }

    public void uninstallHooks() {
        try {
            textViewer.getTextWidget().removeSelectionListener(selectionChangeListener);
            textViewer.getTextWidget().removeCaretListener(caretListener);
            textViewer.getTextWidget().removePaintListener(visualCaretPainter);
            textViewer.getSelectionProvider().removeSelectionChangedListener(selectionChangeListener);

            // This extra check is necessary because the document we're operating on might be a
            // different one from the one the textViewer contained during installHooks() invocation.
            // This document swapping happens if the editor input changed, and we're only triggered
            // *after* the textViewer changed its document instance.
            if (textViewer.getDocument().containsPositionCategory(POSITION_CATEGORY_NAME)) {
                textViewer.getDocument().removePositionCategory(POSITION_CATEGORY_NAME);
            }
        } catch (RuntimeException e) {
            throw new VrapperPlatformException("Failed to unhook selection listeners", e);
        } catch (BadPositionCategoryException e) {
            throw new VrapperPlatformException("Failed to delete position category", e);
        }
    }

    @Override
    public Position getPosition() {
        if (selection != null) {
            // It should be rare that we get in this case. The code below assumes visual mode is
            // active. Linewise visual mode has a completely different notion of caret position.
            return selection.getTo();
        }
        Point sel = textViewer.getSelectedRange();
        int caretOffset = textViewer.getTextWidget().getCaretOffset();
        int cursorPos = converter.widgetOffset2ModelOffset(caretOffset);
        // Workaround for inclusive selections where the caret is on the character near the end of
        // the selection, not on the one after the selection.
        if (sel.y > 0 && cursorPos == sel.x + sel.y
                && Selection.INCLUSIVE.equals(configuration.get(Options.SELECTION))) {
            // Do this on widget (view) offset so that we can jump over a closed fold.
            return shiftPositionForViewOffset(caretOffset, -1, true);
        }
        return new TextViewerPosition(textViewer, Space.MODEL, cursorPos);
    }

    @Override
    public void setPosition(final Position position, final StickyColumnPolicy columnPolicy) {
        caretListener.disable();
        // Use our own position, SWT block mode doesn't know about Vrapper's caret position.
        Position oldPosition = getPosition();
        int viewOffset = position.getViewOffset();
        int oldViewOffset = oldPosition.getViewOffset();
        if (viewOffset < 0) {
            VrapperLog.info("Position M" + position.getModelOffset() + " was not visible.");
            //Something went screwy, avoid getting into a bad state.
            //Just put the cursor at offset 0.
            viewOffset = 0;
        }
        try {
            textViewer.getTextWidget().setSelection(viewOffset);
        } catch (IllegalArgumentException e) {
            throw new VrapperPlatformException("Cannot set caret position to V " + viewOffset, e);
        }
        // Reset Vrapper selection
        selection = null;
        switch (columnPolicy) {
        case NEVER:
            break;
        case ON_CHANGE:
            if (oldViewOffset != viewOffset) {
                updateStickyColumn(viewOffset);
            }
            break;
        case RESET_EOL:
            stickToEOL = false;
            updateStickyColumn(viewOffset);
            break;
        case TO_EOL:
            stickToEOL = true;
            break;
        }
        caretListener.enable();
    }

    @Override
    public Position stickyColumnAtViewLine(final int lineNo) {
        // FIXME: do this properly
        final StyledText tw = textViewer.getTextWidget();
        if (!stickToEOL) {
            try {
                final int y = tw.getLocationAtOffset(tw.getOffsetAtLine(lineNo)).y;
                final int offset = tw.getOffsetAtPoint(new Point(stickyColumn - tw.getHorizontalPixel(), y));
                return new TextViewerPosition(textViewer, Space.VIEW, offset);
            } catch (final IllegalArgumentException e) {
                // fall through silently and return line end
            }
        }
        final int line = converter.widgetLine2ModelLine(lineNo);
        try {
            final int lineLen = textViewer.getDocument().getLineLength(line);
            final String nl = textViewer.getDocument().getLineDelimiter(line);
            final int nlLen = nl != null ? nl.length() : 0;
            final int offset = tw.getOffsetAtLine(lineNo) + lineLen - nlLen;
            return new TextViewerPosition(textViewer, Space.VIEW, offset);
        } catch (final BadLocationException e) {
            throw new VrapperPlatformException("Failed to get sticky column for VL" + lineNo
                    + "/ML" + line, e);
        }
    }

    @Override
    public Position stickyColumnAtModelLine(final int lineNo) {
        // FIXME: do this properly
        if (stickToEOL) {
            try {
                final int lineLength = textViewer.getDocument().getLineLength(lineNo);
            	//getLineLength includes the line's delimiter
            	//we need to find the last non-delimiter character
                final int startOffset = textViewer.getDocument().getLineInformation(lineNo).getOffset();
                int endOffset = startOffset + lineLength;
                //don't leave the cursor on a newline
                if(lineLength > 0) {
                	endOffset--;
                }
                //check for multi-byte (windows) line-endings (\r\n)
                if(endOffset > startOffset && VimUtils.isNewLine(textContent.getModelContent().getText(endOffset, 1))) {
                	endOffset--;
                }
                return new TextViewerPosition(textViewer, Space.MODEL, endOffset);
            } catch (final Exception e) {
                throw new VrapperPlatformException("Failed to get sticky column for ML" + lineNo, e);
            }
        } else {
            try {
                return stickyColumnAtViewLine(converter.modelLine2WidgetLine(lineNo));
            } catch (final RuntimeException e) {
                try {
                    final int caretOffset = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
                    final int lineOffset = textViewer.getDocument().getLineInformationOfOffset(caretOffset).getOffset();
                    final int y = Math.abs(caretOffset - lineOffset);
                    final IRegion line = textViewer.getDocument().getLineInformation(lineNo);
                    final int offset = line.getOffset() + Math.min(y, line.getLength());
                    return new TextViewerPosition(textViewer, Space.MODEL, offset);
                } catch (final BadLocationException e1) {
                    throw new VrapperPlatformException("Failed to get sticky column for ML"
                            + lineNo, e);
                }
            }
        }
    }

    @Override
    public Selection getSelection() {
        if (selection != null) {
            return selection;
        }
        TextRange range = getNativeSelection();
        Position from = range.getStart();
        Position to = range.getEnd();
        boolean isInclusive = Selection.INCLUSIVE.equals(configuration.get(Options.SELECTION));
        if (range.getModelLength() > 0 && isInclusive) {
            if (range.isReversed()) {
                from = shiftPositionForViewOffset(from.getViewOffset(), -1, true);
            } else {
                to = shiftPositionForViewOffset(to.getViewOffset(), -1, true);
            }
        }
        return new SimpleSelection(from, to, range);
    }
    
    @Override
    public TextRange getNativeSelection() {
        if (selection != null) {
            return SelectionService.VRAPPER_SELECTION_ACTIVE;
        }
        final Point sel = textViewer.getSelectedRange();
        int start, end;
        start = end = sel.x;
        final int len = sel.y;
        final int pos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
        if (len > 0) {
            if (sel.x == pos) {
                //sel.x is left bound of UI selection and cursor is to the left,
                //so start of the selection should be moved to the right (reversed selection).
                start += len;
            } else {
                end += len;
            }
        }
        final Position startSel = new TextViewerPosition(textViewer, Space.MODEL, start);
        final Position endSel =   new TextViewerPosition(textViewer, Space.MODEL, end);
        return StartEndTextRange.exclusive(startSel, endSel);
    }

    public boolean isSelectionInProgress() {
        return selectionInProgress;
    }

    @Override
    public void setSelection(final Selection newSel) {
        selectionInProgress = true;
        selection = newSel;
        ContentType contentType = newSel == null ? null: newSel.getContentType(configuration);
        if (newSel == null) {
            final Point point = textViewer.getSelectedRange();
            textViewer.getTextWidget().setBlockSelection(false);
            textViewer.setSelectedRange(point.x, 0);
        } else if (ContentType.TEXT_RECTANGLE.equals(contentType)) {
            setBlockSelection(newSel);
        } else {
            int start = newSel.getStart().getModelOffset();
            int length = !newSel.isReversed() ? newSel.getModelLength() : -newSel.getModelLength();

            selection = newSel;
            //Only the caller can set the sticky column, e.g. in the case of an up/down motion.
            caretListener.disable();
            textViewer.getTextWidget().setBlockSelection(false);
            textViewer.setSelectedRange(start, length);
            caretListener.enable();
        }
        selectionInProgress = false;
    }

    private void setBlockSelection(Selection newSelection) {
        final StyledText styled = textViewer.getTextWidget();
        styled.setBlockSelection(true);
        GC gc = new GC(styled);
        final int avgCharWidth = (int)Math.round(gc.getFontMetrics().getAverageCharacterWidth());
        gc.dispose();
        final TextContent viewContent = textContent.getViewContent();
        final int fromOfs = selection.getFrom().getViewOffset();
        final int toOfs = selection.getTo().getViewOffset();
        if (toOfs == textViewer.getDocument().getLength()) {
            // Don't change selection if the caret is after the last character.
            return;
        }
        final Rectangle fromRect = styled.getTextBounds(fromOfs, fromOfs);
        LineInformation fromLine = viewContent.getLineInformationOfOffset(fromOfs);
        final Rectangle toRect = styled.getTextBounds(toOfs, toOfs);
        LineInformation toLine = viewContent.getLineInformationOfOffset(toOfs);
        // getTextBounds actually returns a bounding box for the last character and never for the
        // endline. If either one of these positions is EOL, shift to right
        if (fromLine.getEndOffset() == fromOfs) {
            fromRect.x += avgCharWidth;
        }
        if (toLine.getEndOffset() == toOfs) {
            toRect.x += avgCharWidth;
        }
        Rectangle blockRect;
        if (stickToEOL) {
            int leftTrim;
            if (fromRect.x < toRect.x) {
                leftTrim = fromRect.x;
            } else {
                leftTrim = toRect.x;
            }
            if (fromOfs <= toOfs) {
                // endOfs is always looking at the last character on the current line.
                // The following situations are possible:
                // startOfs -> o                (pos on top of EOL)
                //             |
                // leftTrim -> o---------------------o <- endOfs
                // or:
                // startOfs -> o----------------o <- startEOL
                //             |
                // leftTrim -> o------------o <- endOfs
                // or:
                // startOfs --------v
                //             +----o----------o <- startEOL
                //             |
                // leftTrim -> o <- endOfs

                // getTextBounds deals with all cases as it includes everything to right of startOfs
                blockRect = styled.getTextBounds(fromOfs, toOfs);
            } else {
                // leftTrim -> o-------o <-endOfs
                //             |
                // startOfs -> o---------------------o <- startEOL
                // or:
                // leftTrim -> o <-endOfs
                //             |
                //             +--------o------------o <- startEOL
                // startOfs ____________^
                // or:
                // leftTrim -> o <-endOfs
                //             |
                //             +--------o <- startEOL
                // startOfs ____________^

                blockRect = styled.getTextBounds(toOfs, fromLine.getEndOffset());
            }
            // Enlarge block to right to include endline characters (endOfs is always past the line)
            blockRect.width += avgCharWidth;
            // blockRect is now a "spanning block" - it includes characters on the left. Trim it.
            blockRect.width -= leftTrim - blockRect.x;
            // Move block to where we started trimming.
            blockRect.x = leftTrim;
        } else {
            blockRect = fromRect.union(toRect);
        }
        //
        // getTextBounds returns values relative to the top-left visible
        // pixel, adjusting the block rectangle accordingly.
        //
        blockRect.x += styled.getHorizontalPixel();
        blockRect.y += styled.getTopPixel();
        // NOTE: setBlockSelectionBound temporary changes caret offset and
        //       triggers incorrect sticky column recalculation.
        caretListener.disable();
        styled.setBlockSelectionBounds(blockRect);
        caretListener.enable();
    }

    @Override
    public void setNativeSelection(TextRange range) {
        int length = !range.isReversed() ? range.getModelLength() : -range.getModelLength();
        selection = null;
        //Only the caller can set the sticky column, e.g. in the case of an up/down motion.
        caretListener.disable();
        textViewer.getTextWidget().setBlockSelection(false);
        textViewer.setSelectedRange(range.getStart().getModelOffset(), length);
        caretListener.enable();
    }

    @Override
    public Position newPositionForModelOffset(final int offset) {
        return new TextViewerPosition(textViewer, Space.MODEL, offset);
    }

    @Override
    public Position newPositionForViewOffset(final int offset) {
        return new TextViewerPosition(textViewer, Space.VIEW, offset);
    }

    @Override
    public Position newPositionForModelOffset(int targetModelOffset, Position original,
            boolean allowPastLastChar) {
        int modelOffset = original.getModelOffset();
        targetModelOffset = safeAddModelOffset(modelOffset, targetModelOffset, allowPastLastChar);
        return new TextViewerPosition(textViewer, Space.MODEL, targetModelOffset);
    }

    @Override
    public Position shiftPositionForModelOffset(int offset, int delta, boolean allowPastLastChar) {
        offset = safeAddModelOffset(offset, offset + delta, allowPastLastChar);
        return new TextViewerPosition(textViewer, Space.MODEL, offset);
    }

    @Override
    public Position shiftPositionForViewOffset(int offset, int delta, boolean allowPastLastChar) {
        int oldModelOffset = converter.widgetOffset2ModelOffset(offset);
        // This might become a serious leap in case of folds
        int targetOffset = converter.widgetOffset2ModelOffset(offset + delta);
        int shiftedOffset = safeAddModelOffset(oldModelOffset, targetOffset, allowPastLastChar);
        // Stick to model space, again safer for folds
        return new TextViewerPosition(textViewer, Space.MODEL, shiftedOffset);
    }

    @Override
    public void setCaret(final CaretType caretType) {
        StyledText styledText = textViewer.getTextWidget();
        if (this.caretType != caretType) {
            Caret old = styledText.getCaret();
            Rectangle oldBounds = old.getBounds();
            Caret newCaret = CaretUtils.createCaret(caretType, styledText);
            // old caret is not disposed automatically
            old.dispose();
            this.caretType = caretType;
            this.caretCachedSize = newCaret.getSize();
            styledText.setCaret(newCaret);
            // Repaint region to make sure that VisualCaretPainter operates correctly
            styledText.redraw(oldBounds.x, oldBounds.y, oldBounds.width, oldBounds.height, true);
        } else {
            // Reset caret size without disposing - sometimes Eclipse switches to an Insert cursor.
            Caret caret = styledText.getCaret();
            Rectangle oldBounds = caret.getBounds();
            caret.setSize(caretCachedSize);
            // Repaint region where old caret used to be - otherwise artifacts might show up
            styledText.redraw(oldBounds.x, oldBounds.y, oldBounds.width, oldBounds.height, true);
        }
    }

    /**
     * Resets Vrapper's internal cached selection when Eclipse changes the TextViewer selection.
     * <p>Note that {@link SelectionListener} is only triggered for specific cases, JFace commands
     * don't seem to do it. That's why the extra {@link ISelectionChangedListener} monitors those
     * changes.
     */
    private final class SelectionChangeListener implements SelectionListener, ISelectionChangedListener {
        @Override
        public void widgetDefaultSelected(final SelectionEvent arg0) {
            // The StyledText javadoc claims that this is never called.
        }

        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            if ( ! selectionInProgress) {
                selection = null;
                // getPosition() compensates for inclusive visual selection's caret offset.
                Position position = getPosition();
                int viewOffset = position.getViewOffset();
                if (viewOffset >= 0) {
                    updateStickyColumn(viewOffset);
                } else {
                    VrapperLog.error("Cannot update stick column: caret is at position M"
                            + position.getModelOffset() + " but" + " viewoffset is -1!");
                }
            }
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if ( ! selectionInProgress) {
                selection = null;
                // getPosition() compensates for inclusive visual selection's caret offset.
                Position position = getPosition();
                int viewOffset = position.getViewOffset();
                if (viewOffset >= 0) {
                    updateStickyColumn(viewOffset);
                } else {
                    VrapperLog.error("Cannot update stick column: caret is at position M"
                            + position.getModelOffset() + " but" + " viewoffset is -1!");
                }
            }
        }
    }

    private final class StickyColumnUpdater implements CaretListener {

        boolean enabled = true;

        @Override
        public void caretMoved(final CaretEvent e) {
            if (enabled) {
                updateStickyColumn(e.caretOffset);
            }
        }

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
        }

    }

    private final class VisualCaretPainter implements PaintListener {

        @Override
        public void paintControl(PaintEvent e) {
            if ( ! VrapperPlugin.isVrapperEnabled()) {
                return;
            }
            StyledText text = textViewer.getTextWidget();
            // Never move caret somewhere else in Insert / Select mode
            if (text.getSelectionCount() == 0
                    || (vrapperModeRecorder.getCurrentMode() instanceof InsertMode)) {
                return;
            }
            // Forces caret visibility for blockwise mode: normally it is disabled all the time.
            // Regular visual modes should have it visible anyway.
            text.getCaret().setVisible(true);
            Position to = getSelection().getTo();
            boolean isInclusive = Selection.INCLUSIVE.equals(configuration.get(Options.SELECTION));
            int offset = to.getViewOffset();
            int documentLength = textViewer.getDocument().getLength();
            // Selection is on some character in a fold or file is empty?
            if (offset < 0) {
                VrapperLog.debug("In a fold");
                return;
            }
            // Fix caret position for last character in the file - selection.to might not be correct
            if (documentLength > 0 && documentLength == to.getModelOffset() && isInclusive) {
                offset--;
            }
            Point visualOffset = text.getLocationAtOffset(offset);
            text.getCaret().setLocation(visualOffset);
        }
    }

    @Override
    public void updateLastPosition() {
        INavigationHistory history = editorInfo.getCurrent().getSite().getPage().getNavigationHistory();
        INavigationLocation currentLocation = history.getCurrentLocation();
        if (currentLocation != null) {
            currentLocation.update();
        }
    }

    @Override
    public void markCurrentPosition() {
        IEditorPart editorPart = editorInfo.getCurrent();
        // Store current location in Eclipse
        editorPart.getSite().getPage().getNavigationHistory().markLocation(editorPart);
    }

    @Override
    public Set<String> getAllMarks() {
        //the easy part, get all local marks
        Set<String> allMarks = new HashSet<String>(marks.keySet());

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        // recursively find all markers in project root
        try {
            final IMarker[] markers = root.findMarkers(GLOBAL_MARK_TYPE, true, IResource.DEPTH_INFINITE);
            for (final IMarker m: markers) {
                String markName = m.getAttribute(IMarker.MESSAGE, "--");
                if ( ! markName.equals("--")) {
                    allMarks.add(markName);
                }
            }
        } catch (CoreException e) {
            VrapperLog.error("Failed to find markers in resource root " + root, e);
        }

        return allMarks;
    }

    @Override
    public void setMark(final String id, final Position position) {
        if (isGlobalMark(id)) {
            setGlobalMark(id, position);
            return;
        }
        final org.eclipse.jface.text.Position p = new org.eclipse.jface.text.Position(position.getModelOffset());
        try {
            //add listener so Position automatically updates as the document changes
            textViewer.getDocument().addPosition(p);
        } catch (final BadLocationException e) {
            throw new VrapperPlatformException("Failed to set mark for " + position, e);
        }

        if (id == LAST_EDIT_MARK) {
        	changeList.add(p);
        	if(changeList.size() > 100) {
        		//remove (and stop tracking changes for) old positions
        		textViewer.getDocument().removePosition( changeList.remove(0) );
        	}
        	//new edit, restart index position
        	changeListIndex = changeList.size();
        }
        else if (marks.containsKey(id)) {
        	//we're about to overwrite an old position
        	//no need to track its changes anymore
        	textViewer.getDocument().removePosition( marks.get(id) );
        }

        //update mark position
        marks.put(id, p);
    }

    public void deleteMark(String id) {
        if (isGlobalMark(id)) {
            deleteGlobalMark(id);
            return;
        }
        if (marks.containsKey(id)) {
            textViewer.getDocument().removePosition(marks.get(id));
            marks.remove(id);
        }
    }

    /**
     * Returns true if mark id is a global mark name
     */
    @Override
    public boolean isGlobalMark(final String id) {
        return id.length() == 1
                && ((   id.charAt(0) >= 'A' && id.charAt(0) <= 'Z')
                    || (id.charAt(0) >= '0' && id.charAt(0) <= '9'));
    }

    @Override
    public Position getMark(String id) {
        if (isGlobalMark(id)) {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IMarker marker = getGlobalMarker(id, root);
            // Check if marker is in the current file.
            if (marker != null) {
                return getGlobalMarkerPosition(marker);
            }
        }
    	//`` and '' are the same position, so we need to return that position
    	//regardless of which way the user accessed it
    	if(id.equals("`")) {
    		id = LAST_JUMP_MARK;
    	}

        final org.eclipse.jface.text.Position p = marks.get(id);
        if (p == null || p.isDeleted) {
        	if(id.equals(LAST_CHANGE_END)) {
        		//if a change was deleted, '[ and '] are the same position
        		//(for whatever reason, '[ has p.isDeleted = false)
        		return getMark(LAST_CHANGE_START);
        	}
        	//leave deleted entries in marks Map
        	//in case an 'undo' brings it back
            return null;
        }
        final int offset = p.getOffset();
        return newPositionForModelOffset(offset);
    }

    /**
     * Lookup the specified marker recursively starting at @a resource.
     * Use @a ResourcesPlugin.getWorkspace().getRoot() to find the marker globally.
     * @param id marker name
     * @param resource resource node.
     * @return marker or @a null if not found.
     */
    static public IMarker getGlobalMarker(String id, IResource resource) {
        try {
            final IMarker[] markers = resource.findMarkers(GLOBAL_MARK_TYPE, true, IResource.DEPTH_INFINITE);
            for (final IMarker m: markers) {
                if (m.getAttribute(IMarker.MESSAGE, "--").equals(id)) {
                    return m;
                }
            }
        } catch (CoreException e) {
            // Ignore.
            VrapperLog.error("Failed to find markers in resource " + resource, e);
        }
        return null;
    }

    /**
     * Creates a global bookmark for the specified position.
     * @param name bookmark name
     * @param position editor position for the bookmark.
     */
    private void setGlobalMark(String name, Position position) {
        final IEditorPart editorPart =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editorPart != null) {
            final IEditorInput editorInput = editorPart.getEditorInput();
            if (!(editorInput instanceof IFileEditorInput)) {
                // Ignore editors without files.
                return;
            }
            final IFileEditorInput fileInput = (IFileEditorInput)editorInput;
            final IFile file = fileInput.getFile();
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            try {
                final IMarker marker = getGlobalMarker(name, root);
                if (marker != null) {
                    marker.delete();
                }
                final HashMap<String, Object> map = new HashMap<String, Object>();
                MarkerUtilities.setMessage(map, name);
                final int line = textViewer.getDocument().getLineOfOffset(position.getModelOffset());
                MarkerUtilities.setLineNumber(map, line);
                MarkerUtilities.setCharStart(map, position.getModelOffset());
                MarkerUtilities.setCharEnd(map, position.getModelOffset() + 1);
                MarkerUtilities.createMarker(file, map, GLOBAL_MARK_TYPE);
            } catch (BadLocationException e) {
                throw new VrapperPlatformException("Failed to set global mark for " + position, e);
            } catch (CoreException e) {
                VrapperLog.error("Failed to set marker in editor input " + editorInput, e);
            }
        }
    }
    private void deleteGlobalMark(String name) {
        final IEditorPart editorPart =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editorPart != null) {
            final IEditorInput editorInput = editorPart.getEditorInput();
            if (!(editorInput instanceof IFileEditorInput)) {
                // Ignore editors without files.
                return;
            }
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            try {
                final IMarker marker = getGlobalMarker(name, root);
                if (marker != null) {
                    marker.delete();
                }
            } catch (CoreException e) {
                VrapperLog.error("Failed to set marker in editor input " + editorInput, e);
            }
        }
    }

    private Position getGlobalMarkerPosition(IMarker marker) {
        int start = MarkerUtilities.getCharStart(marker);
        final IEditorPart editorPart =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

        if (editorPart != null) {
            if (editorPart instanceof AbstractTextEditor) {
                final AbstractTextEditor editor = (AbstractTextEditor) editorPart;
                final IAnnotationModel annotationModel =
                        editor.getDocumentProvider().getAnnotationModel(editorPart.getEditorInput());
                if (annotationModel instanceof AbstractMarkerAnnotationModel) {
                    final AbstractMarkerAnnotationModel markerModel= (AbstractMarkerAnnotationModel) annotationModel;
                    final org.eclipse.jface.text.Position pos = markerModel.getMarkerPosition(marker);
                    if (pos != null && !pos.isDeleted()) {
                        // Use the position instead of marker offset.
                        start = pos.getOffset();
                    } else {
                        // Do nothing if the position has been deleted or the
                        // marker is not in the current file.
                        return null;
                    }
                }

            }
        }
        return newPositionForModelOffset(start);
    }

    @Override
    public Position getNextChangeLocation(final int count) {
    	final int index = changeListIndex + count;
    	return getChangeLocation(index);
    }

    @Override
    public Position getPrevChangeLocation(final int count) {
    	final int index = changeListIndex - count;
    	return getChangeLocation(index);
    }

    private Position getChangeLocation(int index) {
    	if(changeList.size() == 0) {
    		return null;
    	}

    	if(index < 0) {
    		index = 0;
    	}
    	else if(index >= changeList.size()) {
    		index = changeList.size() -1;
    	}

    	final org.eclipse.jface.text.Position p = changeList.get(index);
    	if(p == null || p.isDeleted) {
    		changeList.remove(index);
    		changeListIndex = changeList.size();
    		if(p != null) { //deleted
    			textViewer.getDocument().removePosition(p);
    		}
    		return null;
    	}
    	else {
    		changeListIndex = index; //prepare for next invocation
    		return newPositionForModelOffset(p.getOffset());
    	}
    }

    @Override
    public int getVisualOffset(Position position) {
        final int offset = position.getViewOffset();
        StyledText textWidget = textViewer.getTextWidget();
        int visualOffset = textWidget.getLocationAtOffset(offset).x + textWidget.getHorizontalPixel();

        return visualOffset;
    }

    @Override
    public Position getPositionByVisualOffset(int lineNo, int visualOffset) {
        final StyledText tw = textViewer.getTextWidget();
        final int relVOffset = visualOffset - tw.getHorizontalPixel();
        try {
            final IRegion region = textViewer.getDocument().getLineInformation(lineNo);
            final int lineOffset = converter.modelOffset2WidgetOffset(region.getOffset());
            if (region.getLength() == 0) {
                final Point lineStartPos = tw.getLocationAtOffset(lineOffset);
                if (relVOffset == lineStartPos.x) {
                    // Beginning of an empty line.
                    return new TextViewerPosition(textViewer, Space.VIEW, lineOffset);
                } else {
                    return null;
                }
            }
            final Rectangle lineBounds = tw.getTextBounds(lineOffset, lineOffset + region.getLength() - 1);
            if (!lineBounds.contains(relVOffset, lineBounds.y)) {
                return null;
            }
            //
            // Guess the offset by text width. The calculation is accurate if
            // there are no <TAB> characters on the line.
            //
            int offset = lineOffset + (region.getLength() * visualOffset) / lineBounds.width;
            //
            // Check the guess and adjust is offset if missed.
            //
            Rectangle rect;
            while (!(rect = tw.getTextBounds(offset, offset)).contains(relVOffset, rect.y)) {
                if (rect.x > relVOffset) {
                    --offset;
                } else {
                    ++offset;
                }
            }
            return new TextViewerPosition(textViewer, Space.VIEW, offset);
        } catch (BadLocationException e1) {
            // No character at the specified visual offset.
            return null;
        }
    }

    @Override
    public int visualWidthToChars(int visualWidth) {
        return visualWidth / averageCharWidth;
    }

    private void updateStickyColumn(final int offset) {
        StyledText textWidget = textViewer.getTextWidget();
        Point locationAtOffset;
        try {
            locationAtOffset = textWidget.getLocationAtOffset(offset);
        } catch (IllegalArgumentException e) {
            throw new VrapperPlatformException("Failed to get location info for V" + offset, e);
        }
        stickyColumn = locationAtOffset.x + textWidget.getHorizontalPixel();
        final LineInformation line = textContent.getViewContent().getLineInformationOfOffset(offset);
        if (stickToEOL && offset < line.getEndOffset()) {
            stickToEOL = false;
        }
    }

    private int safeAddModelOffset(int oldOffset, int targetOffset, boolean allowPastLastChar) {
        int delta = targetOffset - oldOffset;
        if (delta == 0) {
            return oldOffset;
        } else if (targetOffset <= 0) {
            return 0;
        }
        int contentlength = textViewer.getDocument().getLength();
        if (targetOffset > contentlength) {
            // Clip to text end, but 'onNewline' still might need corrections
            targetOffset = contentlength;
        }
        int line;
        try {
            line = textViewer.getDocument().getLineOfOffset(targetOffset);
        } catch (BadLocationException e) {
            throw new VrapperPlatformException("Failed to get line nr for M" + targetOffset, e);
        }
        int totalLineLength;
        try {
            totalLineLength = textViewer.getDocument().getLineLength(line);
        } catch (BadLocationException e) {
            throw new VrapperPlatformException("Failed to get line length for M" + targetOffset, e);
        }
        IRegion lineInfo;
        try {
            lineInfo = textViewer.getDocument().getLineInformation(line);
        } catch (BadLocationException e) {
            throw new VrapperPlatformException("Failed to get line info for M" + targetOffset, e);
        }
        int beginOffset = lineInfo.getOffset();
        int endOffset = lineInfo.getOffset() + lineInfo.getLength();
        if (targetOffset == contentlength) {
            // Fall through to 'onNewline' check to see if cursor needs to move to the left.
        } else if (delta > 0 && targetOffset > endOffset) {
            // Moving to right but we fall just outside the line. Jump to beginning of next line.
            targetOffset = beginOffset + totalLineLength;
        } else if (delta < 0 && targetOffset > endOffset) {
            // Moving to left but we fall just outside the line. Clip to end of line.
            targetOffset = endOffset;
        }
        if ( ! allowPastLastChar && targetOffset == endOffset
                && targetOffset > beginOffset) {
            // Past last character and the line isn't empty. Move one back.
            targetOffset--;
        }
        return targetOffset;
    }

    @Override
    public boolean shouldStickToEOL() {
        return stickToEOL;
    }
}
