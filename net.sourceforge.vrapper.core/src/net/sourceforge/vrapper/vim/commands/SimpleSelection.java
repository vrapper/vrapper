package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public class SimpleSelection implements Selection {

    private final TextRange range;

    public SimpleSelection(TextRange range) {
        super();
        this.range = range;
    }
    
    @Override
    public String getModeName() {
        return VisualMode.NAME;
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
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

    public Position getFrom() {
        return range.getStart();
    }

    public Position getTo() {
        return range.getEnd();
    }

    @Override
    public Position getStartMark(EditorAdaptor defaultEditorAdaptor) {
        return range.getLeftBound();
    }

    @Override
    public Position getEndMark(EditorAdaptor defaultEditorAdaptor) {
        return range.getRightBound().addModelOffset(-1);
    }

    public String toString() {
        return "SimpleSelection( " + range.toString() + " )";
    }
}
