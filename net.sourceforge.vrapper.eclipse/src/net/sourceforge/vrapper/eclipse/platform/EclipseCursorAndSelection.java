package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.ui.CaretUtils;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.commands.Selection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;

public class EclipseCursorAndSelection implements CursorService, SelectionService {

    private final ITextViewer textViewer;
    private int stickyColumn;
    private boolean stickToEOL = false;
    private final ITextViewerExtension5 converter;
    private Selection selection;
    private final SelectionChangeListener selectionChangeListener;

    public EclipseCursorAndSelection(ITextViewer textViewer) {
        this.textViewer = textViewer;
        converter = OffsetConverter.create(textViewer);
        selectionChangeListener = new SelectionChangeListener();
        textViewer.getTextWidget().addSelectionListener(selectionChangeListener);
    }

    public Position getPosition() {
        return new TextViewerPosition(textViewer, Space.VIEW, textViewer.getTextWidget().getCaretOffset());
    }

    public void setPosition(Position position, boolean updateColumn) {
        textViewer.getTextWidget().setSelection(position.getViewOffset());
        if (updateColumn) {
            stickToEOL = false;
            stickyColumn = textViewer.getTextWidget().getLocationAtOffset(position.getViewOffset()).x;
        }
    }

    public Position stickyColumnAtViewLine(int lineNo) {
        StyledText tw = textViewer.getTextWidget();
        if (!stickToEOL) {
            try {
                int y = tw.getLocationAtOffset(tw.getOffsetAtLine(lineNo)).y;
                int offset = tw.getOffsetAtLocation(new Point(stickyColumn, y));
                return new TextViewerPosition(textViewer, Space.VIEW, offset);
            } catch (IllegalArgumentException e) {
                // fall through silently and return line end
            }
        }
        int lineLen = tw.getLine(lineNo).length();
        int offset = tw.getOffsetAtLine(lineNo) + lineLen;
        return new TextViewerPosition(textViewer, Space.VIEW, offset);
    }

    public Position stickyColumnAtModelLine(int lineNo) {
        if (stickToEOL) {
            try {
                int lineLength = textViewer.getDocument().getLineLength(lineNo);
                int offset = textViewer.getDocument().getLineInformation(lineNo).getOffset() + lineLength;
                return new TextViewerPosition(textViewer, Space.MODEL, offset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return stickyColumnAtViewLine(converter.modelLine2WidgetLine(lineNo));
            } catch (RuntimeException e) {
                try {
                    int caretOffset = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
                    int lineOffset = textViewer.getDocument().getLineInformationOfOffset(caretOffset).getOffset();
                    int y = Math.abs(caretOffset - lineOffset);
                    IRegion line = textViewer.getDocument().getLineInformation(lineNo);
                    int offset = line.getOffset() + Math.min(y, line.getLength());
                    return new TextViewerPosition(textViewer, Space.MODEL, offset);
                } catch (BadLocationException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    public Selection getSelection() {
        if (selection != null) {
            return selection;
        }
        int start, end, pos, len;
        start = end = textViewer.getSelectedRange().x;
        len = textViewer.getSelectedRange().y;
        pos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
            if (start == pos) {
                start += len+1;
            } else {
                end += len;
            }


        Position from = new TextViewerPosition(textViewer, Space.MODEL, start);
        Position to =   new TextViewerPosition(textViewer, Space.MODEL, end);
        return new Selection(new StartEndTextRange(from, to), ContentType.TEXT);
    }

    public void setSelection(Selection newSelection) {
        if (newSelection == null) {
            int cursorPos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
            textViewer.setSelectedRange(cursorPos, 0);
            selection = null;
        } else {
            textViewer.getTextWidget().setCaretOffset(newSelection.getStart().getViewOffset());
            int from = newSelection.getStart().getModelOffset();
            int length = !newSelection.isReversed() ? newSelection.getModelLength() : -newSelection.getModelLength();
            // linewise selection includes final newline, this means the cursor
            // is placed in the line below the selection by eclipse. this
            // corrects that behaviour
            if (ContentType.LINES.equals(newSelection.getContentType())) {
                if (newSelection.isReversed()) {
                    from -= 1;
                    length += 1;
                } else {
                    length -=1;
                }
            }
            selection = newSelection;
            selectionChangeListener.disable();
            textViewer.setSelectedRange(from, length);
            selectionChangeListener.enable();
        }
    }

    public Position newPositionForModelOffset(int offset) {
        return new TextViewerPosition(textViewer, Space.MODEL, offset);
    }

    public Position newPositionForViewOffset(int offset) {
        return new TextViewerPosition(textViewer, Space.VIEW, offset);
    }

    public void setCaret(CaretType caretType) {
        StyledText styledText = textViewer.getTextWidget();
        styledText.setCaret(CaretUtils.createCaret(caretType, styledText));
    }

    public void stickToEOL() {
        stickToEOL = true;
    }

    private final class SelectionChangeListener implements SelectionListener {
        boolean enabled = true;
        public void widgetDefaultSelected(SelectionEvent arg0) {
            if (enabled) {
                selection = null;
            }
        }

        public void widgetSelected(SelectionEvent arg0) {
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


}
