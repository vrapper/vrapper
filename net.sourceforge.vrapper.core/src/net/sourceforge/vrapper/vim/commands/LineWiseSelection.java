package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class LineWiseSelection implements Selection {

    private final Position from;
    private final Position to;
    private final TextRange range;

    public LineWiseSelection(EditorAdaptor editor, Position from, Position to) {
        super();
        this.from = from;
        this.to = to;
        this.range = createRange(editor, from, to);
    }

    public TextRange getRegion(EditorAdaptor editorMode, int count)
            throws CommandExecutionException {
        return range;
    }

    public int getCount() {
        return 1;
    }

    public TextObject withCount(int count) {
        return this;
    }

    public Position getEnd() {
        return range.getEnd();
    }

    public Position getLeftBound() {
        return range.getLeftBound();
    }

    public int getModelLength() {
        return range.getModelLength();
    }

    public Position getRightBound() {
        return range.getRightBound();
    }

    public Position getStart() {
        return range.getStart();
    }

    public int getViewLength() {
        return range.getViewLength();
    }

    public boolean isReversed() {
        return range.isReversed();
    }
    public ContentType getContentType() {
        return ContentType.LINES;
    }
    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    private static TextRange createRange(EditorAdaptor editor, Position start, Position end) {
        TextContent vc = editor.getViewContent();
        LineInformation sLine = vc.getLineInformationOfOffset(start.getViewOffset());
        LineInformation eLine = vc.getLineInformationOfOffset(end.getViewOffset());
        CursorService cs = editor.getCursorService();
        if (sLine.getNumber() < eLine.getNumber()) {
            int endIndex = eLine.getNumber() < vc.getNumberOfLines()
                    ? vc.getLineInformation(eLine.getNumber()+1).getBeginOffset()
                    : eLine.getEndOffset();
            return new StartEndTextRange(
                    cs.newPositionForViewOffset(sLine.getBeginOffset()),
                    cs.newPositionForViewOffset(endIndex));
        } else {
            int startIndex = sLine.getNumber() < vc.getNumberOfLines()
                    ? vc.getLineInformation(sLine.getNumber()+1).getBeginOffset()
                    : sLine.getEndOffset();
            return new StartEndTextRange(
                    cs.newPositionForViewOffset(startIndex),
                    cs.newPositionForViewOffset(eLine.getBeginOffset()));
        }
    }


}
