package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class StartEndTextRange implements TextRange {

    private final Position start;
    private final Position end;

    // TODO: make private and use appropriate static methods
    public StartEndTextRange(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public Position getLeftBound() {
        return !isReversed() ? getStart() : getEnd();
    }

    public Position getRightBound() {
        return !isReversed() ? getEnd() : getStart();
    }

    public int getModelLength() {
        return Math.abs(getSignedModelLength());
    }

    public int getViewLength() {
        return Math.abs(getEnd().getViewOffset() - getStart().getViewOffset());
    }

    public boolean isReversed() {
        return getSignedModelLength() < 0;
    }

    private int getSignedModelLength() {
        return getEnd().getModelOffset() - getStart().getModelOffset();
    }

    public static TextRange lines(EditorAdaptor editor, Position from, Position to) {
        TextContent txt = editor.getModelContent();
        LineInformation sLine = txt.getLineInformationOfOffset(from.getModelOffset());
        LineInformation eLine = txt.getLineInformationOfOffset(to.getModelOffset());
        if (sLine.getNumber() > eLine.getNumber())
            return lines(editor, to, from);
        CursorService cs = editor.getCursorService();
        int startIndex = sLine.getBeginOffset();
        int nextLineNo = eLine.getNumber() + 1;
        int endIndex;
        if (nextLineNo < txt.getNumberOfLines())
            endIndex = txt.getLineInformation(nextLineNo).getBeginOffset();
        else {
            endIndex = txt.getTextLength();
            if (sLine.getNumber() > 0)
                startIndex = txt.getLineInformation(sLine.getNumber() - 1).getEndOffset();
            else
                startIndex = 0;
        }
        return new StartEndTextRange(
                cs.newPositionForModelOffset(startIndex),
                cs.newPositionForModelOffset(endIndex));
    }

    public static TextRange exclusive(Position from, Position to) {
        return new StartEndTextRange(from, to);
    }

    public static TextRange inclusive(Position from, Position to) {
        if (from.getModelOffset() <= to.getModelOffset())
            return new StartEndTextRange(from, to.addModelOffset(1));
        else
            return new StartEndTextRange(from.addModelOffset(1), to);
    }

}
