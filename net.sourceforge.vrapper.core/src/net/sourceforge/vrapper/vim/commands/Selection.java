package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface Selection extends TextObject, TextRange {
    public boolean isReversed();
    public Position getFrom();
    public Position getTo();
    public Position getStartMark(EditorAdaptor defaultEditorAdaptor);
    public Position getEndMark(EditorAdaptor defaultEditorAdaptor);
    /** Returns the name of the mode in which this selection was made. */
    public String getModeName();
}
