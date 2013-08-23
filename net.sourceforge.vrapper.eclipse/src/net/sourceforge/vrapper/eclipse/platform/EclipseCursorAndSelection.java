package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.eclipse.ui.CaretUtils;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

@SuppressWarnings("restriction")
public class EclipseCursorAndSelection implements CursorService, SelectionService {

    public static final String POSITION_CATEGORY_NAME = "net.sourceforge.vrapper.position";
    /// Marker type for global (A-Z0-9) marks. Use IMarker.MARKER to make them invisible.
    public static final String GLOBAL_MARK_TYPE = IMarker.BOOKMARK;
    private final ITextViewer textViewer;
    private int stickyColumn;
    private boolean stickToEOL = false;
    private final ITextViewerExtension5 converter;
    private Selection selection;
    private final SelectionChangeListener selectionChangeListener;
    private final StickyColumnUpdater caretListener;
    private final Map<String, org.eclipse.jface.text.Position> marks;
    private final List<org.eclipse.jface.text.Position> changeList;
    private int changeListIndex;
    private final Configuration configuration;
    private final EclipseTextContent textContent;
    private int averageCharWidth;
    private CaretType caretType = null;

	public EclipseCursorAndSelection(final Configuration configuration,
			final ITextViewer textViewer, final EclipseTextContent textContent) {
        this.configuration = configuration;
        this.textViewer = textViewer;
        this.textContent = textContent;
        StyledText tw = textViewer.getTextWidget();
        this.stickyColumn = tw.getLeftMargin();
        GC gc = null;
        try {
            gc = new GC(tw);
            averageCharWidth = gc.getFontMetrics().getAverageCharWidth();
        } finally {
            if (gc != null)
                gc.dispose();
        }
        converter = OffsetConverter.create(textViewer);
        selectionChangeListener = new SelectionChangeListener();
        caretListener = new StickyColumnUpdater();
        marks = new HashMap<String, org.eclipse.jface.text.Position>();
        changeList = new ArrayList<org.eclipse.jface.text.Position>();
        textViewer.getTextWidget().addSelectionListener(selectionChangeListener);
        textViewer.getTextWidget().addCaretListener(caretListener);
        textViewer.getDocument().addPositionCategory(POSITION_CATEGORY_NAME);
    }

    @Override
    public Position getPosition() {
    	Point sel = textViewer.getSelectedRange();
    	int carretOffset = textViewer.getTextWidget().getCaretOffset();
    	int cursorPos = converter.widgetOffset2ModelOffset(carretOffset);
    	if (sel.y > 0 && cursorPos == sel.x + sel.y) {
    		--cursorPos;
    	}
    	return new TextViewerPosition(textViewer, Space.MODEL, cursorPos);
    }

    @Override
    public void setPosition(final Position position, final boolean updateColumn) {
    	if (!updateColumn) {
	    	caretListener.disable();
    	}
    	int viewOffset = position.getViewOffset();
    	if (viewOffset < 0) {
    		//Something went screwy, avoid getting into a bad state.
    		//Just put the cursor at offset 0.
    		viewOffset = 0;
    	}
        textViewer.getTextWidget().setSelection(viewOffset);
        caretListener.enable();
    }

    @Override
    public Position stickyColumnAtViewLine(final int lineNo) {
        // FIXME: do this properly
        final StyledText tw = textViewer.getTextWidget();
        if (!stickToEOL) {
            try {
                final int y = tw.getLocationAtOffset(tw.getOffsetAtLine(lineNo)).y;
                final int offset = tw.getOffsetAtLocation(new Point(stickyColumn - tw.getHorizontalPixel(), y));
                return new TextViewerPosition(textViewer, Space.VIEW, offset);
            } catch (final IllegalArgumentException e) {
                // fall through silently and return line end
            }
        }
        try {
            final int line = converter.widgetLine2ModelLine(lineNo);
            final int lineLen = textViewer.getDocument().getLineLength(line);
            final String nl = textViewer.getDocument().getLineDelimiter(line);
            final int nlLen = nl != null ? nl.length() : 0;
            final int offset = tw.getOffsetAtLine(lineNo) + lineLen - nlLen;
            return new TextViewerPosition(textViewer, Space.VIEW, offset);
        } catch (final BadLocationException e) {
            throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    @Override
    public Selection getSelection() {
        if (selection != null) {
            return selection;
        }
        final Point sel = textViewer.getSelectedRange();
        int start, end;
        start = end = sel.x;
        final int len = sel.y;
        if (len > 0) {
            final int pos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
            if (start == pos) {
                start += len+1;
            } else {
                end += len;
            }
        }

        final Position from = new TextViewerPosition(textViewer, Space.MODEL, start);
        final Position to =   new TextViewerPosition(textViewer, Space.MODEL, end);
        return new SimpleSelection(new StartEndTextRange(from, to));
    }

    @Override
    public void setSelection(final Selection newSelection) {
        if (newSelection == null) {
            final Point point = textViewer.getSelectedRange();
            textViewer.getTextWidget().setBlockSelection(false);
            textViewer.setSelectedRange(point.x, 0);
            selection = null;
        } else {
            final int from = newSelection.getStart().getModelOffset();
            int length = !newSelection.isReversed() ? newSelection.getModelLength() : -newSelection.getModelLength();
            // linewise selection includes final newline, this means the cursor
            // is placed in the line below the selection by eclipse. this
            // corrects that behaviour
            if (ContentType.LINES.equals(newSelection.getContentType(configuration))) {
                if (!newSelection.isReversed()) {
                    length -=1;
                }
            }
            selection = newSelection;
            selectionChangeListener.disable();
            if (ContentType.TEXT_RECTANGLE.equals(newSelection.getContentType(configuration))) {
                // block selection
                final StyledText styled = textViewer.getTextWidget();
                styled.setBlockSelection(true);
                final int starOfs = selection.getFrom().getViewOffset();
                final int endOfs = selection.getTo().getViewOffset();
                final Rectangle fromRect = styled.getTextBounds(starOfs, starOfs);
                final Rectangle toRect = styled.getTextBounds(endOfs, endOfs);
                final Rectangle blockRect = fromRect.union(toRect);
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
            } else {
                textViewer.getTextWidget().setBlockSelection(false);
                textViewer.setSelectedRange(from, length);
            }
            selectionChangeListener.enable();
        }
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
    public void setCaret(final CaretType caretType) {
        if (this.caretType != caretType) {
            final StyledText styledText = textViewer.getTextWidget();
            final Caret old = styledText.getCaret();
            styledText.setCaret(CaretUtils.createCaret(caretType, styledText));
            // old caret is not disposed automatically
            old.dispose();
            this.caretType = caretType;
        }
    }

    @Override
    public void stickToEOL() {
        stickToEOL = true;
    }

    @Override
    public void stickToBOL() {
        stickToEOL = false;
        int carretOffset = textViewer.getTextWidget().getCaretOffset();
        updateStickyColumn(carretOffset);
    }

    private final class SelectionChangeListener implements SelectionListener {
        boolean enabled = true;
        @Override
        public void widgetDefaultSelected(final SelectionEvent arg0) {
            if (enabled) {
                selection = null;
            }
        }

        @Override
        public void widgetSelected(final SelectionEvent arg0) {
            if (enabled) {
                selection = null;
            }
        }

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
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
			e.printStackTrace();
			return;
		}

        if(id == LAST_EDIT_MARK) {
        	changeList.add(p);
        	if(changeList.size() > 100) {
        		//remove (and stop tracking changes for) old positions
        		textViewer.getDocument().removePosition( changeList.remove(0) );
        	}
        	//new edit, restart index position
        	changeListIndex = changeList.size();
        }
        else if(marks.containsKey(id)) {
        	//we're about to overwrite an old position
        	//no need to track its changes anymore
        	textViewer.getDocument().removePosition( marks.get(id) );
        }

        //update mark position
        marks.put(id, p);
    }

    /**
     * Returns true if mark id is a global mark name
     */
    static public boolean isGlobalMark(final String id) {
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
            } catch (Exception e) {
            }
        }
    }

    /**
     * Finds an editor associated with the specified mark.
     * @param name mark name
     * @return IEditorPart or null if not found.
     */
    static public IEditorPart getGlobalMarkEditor(String name) {
        final WorkbenchPage page = (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorReference[] editorReferences = page.getSortedEditors();
        for (final IEditorReference e : editorReferences) {
            try {
                IEditorInput editorInput = e.getEditorInput();
                if (editorInput instanceof IFileEditorInput) {
                    final IFileEditorInput fileInput = (IFileEditorInput)editorInput;
                    if (getGlobalMarker(name, fileInput.getFile()) != null) {
                        return (IEditorPart) e.getPart(true);
                    }
                }
            } catch (PartInitException e1) {
            }
        }
        return null;
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
        try {
            final IRegion region = textViewer.getDocument().getLineInformation(lineNo);
            if (region.getLength() == 0) {
                return null;
            }
            final int lineOffset = converter.modelOffset2WidgetOffset(region.getOffset());
            final Rectangle lineBounds = tw.getTextBounds(lineOffset, lineOffset + region.getLength());
            if (!lineBounds.contains(visualOffset, lineBounds.y)) {
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
            while (!(rect = tw.getTextBounds(offset, offset)).contains(visualOffset, rect.y)) {
                if (rect.x > visualOffset) {
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
        stickyColumn = textWidget.getLocationAtOffset(offset).x + textWidget.getHorizontalPixel();
        final LineInformation line = textContent.getViewContent().getLineInformationOfOffset(offset);
        if (stickToEOL && offset < line.getEndOffset()) {
            stickToEOL = false;
        }
    }

}
