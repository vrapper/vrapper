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
    public Selection wrap(EditorAdaptor adaptor, TextRange range) {
        // [FIXME] This is incorrect for inc/exclusive mode.
        return new LineWiseSelection(adaptor, range.getStart(), range.getEnd());
    }
}
