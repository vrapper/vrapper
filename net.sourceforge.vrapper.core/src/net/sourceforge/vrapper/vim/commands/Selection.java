package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface Selection extends TextObject, TextRange {

    public abstract boolean isReversed();

    public abstract int getViewLength();

    public abstract Position getStart();

    public abstract Position getRightBound();

    public abstract int getModelLength();

    public abstract Position getLeftBound();

    public abstract Position getEnd();

    public abstract TextObject withCount(int count);

    public abstract int getCount();

    public abstract TextRange getRegion(EditorAdaptor editorMode, int count)
            throws CommandExecutionException;

    public abstract ContentType getContentType();

    public abstract Position getFrom();

    public abstract Position getTo();
}
