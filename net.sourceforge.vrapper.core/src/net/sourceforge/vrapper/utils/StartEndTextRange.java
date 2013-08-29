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
        
        CursorService cs = editor.getCursorService();
        int firstLine, nextLineTolastLine;
        if (sLine.getNumber() > eLine.getNumber()) {
            firstLine = eLine.getNumber();
            nextLineTolastLine = sLine.getNumber() + 1;
        }
        else {
            firstLine = sLine.getNumber();
            nextLineTolastLine = eLine.getNumber() + 1;
        }
        
        int beginOffset = txt.getLineInformation(firstLine).getBeginOffset(), endOffset;
        if (nextLineTolastLine < txt.getNumberOfLines())
            endOffset = txt.getLineInformation(nextLineTolastLine).getBeginOffset();
        else {
            endOffset = txt.getTextLength();
            if (firstLine == 0)
                beginOffset = 0;
        }
        
        if (sLine.getNumber() > eLine.getNumber()) {
            return new StartEndTextRange(
                    cs.newPositionForModelOffset(endOffset),
                    cs.newPositionForModelOffset(beginOffset));
        }
        else {
            return new StartEndTextRange(
                    cs.newPositionForModelOffset(beginOffset),
                    cs.newPositionForModelOffset(endOffset));
        }
    }

    /** Only for debugging. */
    public String toString() {
        return "StartEndTextRange(M " + start.getModelOffset() + "/" + start.getViewOffset()
                + " V - M " + end.getModelOffset() + "/" + end.getViewOffset() + " V)";
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
