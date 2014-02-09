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
    /** Returns a <b>new</b> Selection object of the same type as the current one.
     * Start and end are to be interpreted as the values returned by StartMark and EndMark.
     * If the current selection is reversed, the new one will be as well.
     * <p>If the selection must be visible, the caller is responsible for doing that.
     */
    public Selection selectMarks(EditorAdaptor adaptor, Position start, Position end);
}
