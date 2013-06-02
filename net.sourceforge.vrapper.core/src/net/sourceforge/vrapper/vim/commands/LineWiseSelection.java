package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;

public class LineWiseSelection implements Selection {

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

}
