package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.TextObject;

/**
 * TextObject representing a range of text in model line notation. LineRanges are non-directional,
 * the start line always has a lower number or is equal to the end line.
 * <p>This is a value type, it should not be cached beyond a single {@link Command} execution.
 */
public interface LineRange extends TextObject {
    /** Number of the first complete line in this range. */
    public int getStartLine();
    /** Position on start line. */
    public Position getFrom();
    /** Position on end line. Inclusive. */
    public Position getTo();
    /** Number of the last complete line in this range. */
    public int getEndLine();
    /**
     * Number of characters spanned between <code>startLine</code> and <code>endLine</code>
     * including the final newline of <code>endLine</code> if there is one.
     * <p>This is commonly used to do a replace operation and makes sure that this non-obvious
     * calculation does not need to be copy-pasted all over the codebase. Using {@link
     * #getRegion(net.sourceforge.vrapper.vim.EditorAdaptor, int)} gives the same length, only this
     * method should be able to cache the information.
     */
    public int getModelLength();
    /**
     * Get the range of text from the beginning of start line including the newline at the end of
     * end line (in other words: beginning of <code>end line + 1</code>).
     * @return a {@link TextRange} instance.
     */
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;
}
