package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;

public class LineWiseSelection extends AbstractSelection {

    private final Position from;
    private final Position to;
    private final TextRange range;

    public LineWiseSelection(EditorAdaptor editor, Position from, Position to) {
        super();
        this.from = from;
        this.to = to;
        this.range = StartEndTextRange.lines(editor, from, to);
    }

    public LineWiseSelection(Position selectionStartCaretPos, Position selectionEndCaretPos,
            TextRange range){
        this.range = range;
        this.from = selectionStartCaretPos;
        this.to = selectionEndCaretPos;
    }

    @Override
    public String getModeName() {
        return LinewiseVisualMode.NAME;
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
    public ContentType getContentType(Configuration configuration) {
        return ContentType.LINES;
    }
    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    @Override
    public Position getStartMark(EditorAdaptor editorAdaptor) {
        return range.getLeftBound();
    }

    @Override
    public Position getEndMark(EditorAdaptor editorAdaptor) {
        Position end = range.getRightBound();
        TextContent content = editorAdaptor.getModelContent();
        LineInformation endLine = content.getLineInformationOfOffset(end.getModelOffset());
        int endLineNumber = endLine.getNumber();
        //End position is beginning of next line, move one line back and to end of line unless we
        //are on the last line or if the selection is empty.
        if (range.getModelLength() > 0 && endLine.getBeginOffset() == end.getModelOffset()) {
            CursorService cursorService = editorAdaptor.getCursorService();
            LineInformation prevLine = content.getLineInformation(endLineNumber - 1);
            end = cursorService.newPositionForModelOffset(prevLine.getEndOffset());
        }
        return end;
    }

    @Override
    public Selection doReset(EditorAdaptor adaptor, Position from, Position to) {
        return new LineWiseSelection(adaptor, from, to);
    }

    @Override
    public Selection syncToTextRange(EditorAdaptor adaptor, TextRange range) {
        CursorService cs = adaptor.getCursorService();
        TextContent content = adaptor.getModelContent();
        // [NOTE] There's no difference between exclusive or inclusive selecton in visual line mode
        // Use range ends directly
        Position newFrom = range.getStart();
        Position newTo = range.getEnd();
        LineInformation startLine = content.getLineInformationOfOffset(newFrom.getModelOffset());
        LineInformation endLine = content.getLineInformationOfOffset(newTo.getModelOffset());

        // Handle simple single-line case
        if (startLine.getNumber() == endLine.getNumber()) {
            Position temp = cs.newPositionForModelOffset(startLine.getBeginOffset());
            Position offsetBehindEOL = cs.shiftPositionForModelOffset(startLine.getBeginOffset(),
                    startLine.getLength() + 1, false);
            return new LineWiseSelection(newFrom, newTo, new StartEndTextRange(temp, offsetBehindEOL));

        } else if (range.isReversed() && startLine.getBeginOffset() == newFrom.getModelOffset()) {
            // End-of-line included, the 'from' position is at the end of the previous line.
            newFrom = cs.shiftPositionForViewOffset(newFrom.getViewOffset(), -1, true);
        } else if (endLine.getBeginOffset() == newTo.getModelOffset()) {
            // End-of-line included, the 'to' position is at the end of the previous line.
            newTo = cs.shiftPositionForViewOffset(newTo.getViewOffset(), -1, true);
        }
        return new LineWiseSelection(adaptor, newFrom, newTo);
    }
}
