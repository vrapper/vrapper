package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.ui.CaretUtils;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

public class EclipseCursorAndSelection implements CursorService, SelectionService {

    private final ITextViewer textViewer;
    private int stickyColumn;
    private boolean stickToEOL = false;
    private final ITextViewerExtension5 converter;

    public EclipseCursorAndSelection(ITextViewer textViewer) {
        this.textViewer = textViewer;
        converter = OffsetConverter.create(textViewer);
    }

    public Position getPosition() {
        return new TextViewerPosition(textViewer, Space.VIEW, textViewer.getTextWidget().getCaretOffset());
    }

    public void setPosition(Position position, boolean updateColumn) {
        textViewer.getTextWidget().setCaretOffset(position.getViewOffset());
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

    public TextRange getSelection() {
        int start, end, pos, len;
        start = end = textViewer.getSelectedRange().x;
        len = textViewer.getSelectedRange().y;
        pos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
        if (start == pos) {
            start += len;
        } else {
            end += len;
        }


        Position from = new TextViewerPosition(textViewer, Space.MODEL, start);
        Position to =   new TextViewerPosition(textViewer, Space.MODEL, end);
        return new StartEndTextRange(from, to);
    }

    public void setSelection(TextRange selection) {
        if (selection == null) {
            int cursorPos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
            textViewer.setSelectedRange(cursorPos, 0);
        } else {
            textViewer.getTextWidget().setCaretOffset(selection.getStart().getViewOffset());
            int from = selection.getStart().getModelOffset();
            int length = !selection.isReversed() ? selection.getModelLength() : -selection.getModelLength();
            textViewer.setSelectedRange(from, length);
        }
    }

    public void setLineWiseSelection(boolean lineWise) {
        VrapperLog.error("line wise selection not implemented");
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

}
