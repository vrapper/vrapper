package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface Selection extends TextObject, TextRange {
    /** Whether the selection is from bottom to top and / or right to left. */
    public boolean isReversed();
    /**
     * Returns a character near the start boundary of the selection.
     * 
     * <p>The difference between {@link #getStart()} is subtle to none. It can only be really seen
     * when a {@link LineWiseSelection} is active. In that case, {@link #getFrom()} returns
     * any character on the starting line, whereas {@link #getStart()} will always return the first
     * or last character on that line, depending on whether the selection is reversed or not.
     */
    public Position getFrom();
    /**
     * Returns the position of the "cursor" near the end of the selection. Vim allows to show this
     * "cursor" in line-wise mode but Eclipse can't do it because the caret must always be at either
     * end of the selection.
     * 
     * <p>The difference between {@link #getEnd()} is subtle to none. It can only be really seen
     * when a {@link LineWiseSelection} is active. In that case, {@link #getTo()} returns
     * the position of the "cursor" on the ending line, whereas {@link #getEnd()} will always return
     * the first or last character on that line, depending on whether the selection is reversed or
     * not.
     */
    public Position getTo();
    /** Returns the left- and top-most position included in the selection, even if reversed. */
    public Position getStartMark(EditorAdaptor defaultEditorAdaptor);
    /**
     * Returns the right- and bottom-most position included in the selection, even if reversed.
     * <p>For line-wise selection, this doesn't include the final newline.
     */
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
