package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface Selection extends TextObject, TextRange {
    
    /** Config value of 'selection' option which allows a selection to exclude the cursor (i.e. it
     * can be empty).
     */
    public static final String EXCLUSIVE = "exclusive";

    /** Config value of 'selection' option which makes that a selection always needs to include the
     * cursor (i.e. it must always be at least 1 character long).
     */
    public static final String INCLUSIVE = "inclusive";

    /** Config value of 'selection' option which is allowed for compatiblity with Vim but is not
     * supported.
     */
    public static final String OLD = "old";
    
    public static final String SELECTION_OPTIONS = OLD + ", " + INCLUSIVE + ", " + EXCLUSIVE;
    
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
     * Returns the position of the "cursor" within the selection. Compare this to "effective" end of
     * the selection returned by {@link #getEnd()}.
     * 
     * <p>This is a workaround for the fact that Eclipse must always have its caret at the end of a
     * selection whereas Vim can have it on a character (visual mode and blockwise with
     * "inclusive" selection) or somewhere else on the line (linewise).
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
    /**
     * Returns a <b>new</b> Selection object of the same type as the current one but using the from
     * and to positions corrected for changes in the surrounding text. Exclusive / inclusive mode is
     * handled automatically
     * <p>The caller is responsible for making the selection visible.
     */
    public Selection reset(EditorAdaptor adaptor, Position from, Position to);

    /**
     * Returns a <b>new</b> Selection object of the same type as the current one but using the
     * boundaries given by a text range. The passed in textrange is exclusive, the returned
     * Selection depends on the value of the <code>selection</code> setting.
     * <p> The caller is responsible for making the selection visible.
     */
    public Selection syncToTextRange(EditorAdaptor adaptor, TextRange range);
}
