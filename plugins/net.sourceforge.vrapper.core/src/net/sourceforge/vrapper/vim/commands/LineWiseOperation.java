package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public interface LineWiseOperation {

    /**
     * Apply an operation on a set of lines.
     * 
     * @param editorAdaptor editor reference.
     * @param lineRange {@link LineRange} holding information about the start and ends of the range.
     *      Should not be null!
     */
    public void execute(EditorAdaptor editorAdaptor, LineRange lineRange) throws CommandExecutionException;

    /**
     * Method which calculates the spanned range when no region was given. Examples include the
     * current line or the entire file.
     * @param currentPos Position of the caret in the document.
     * @return a {@link LineRange}. Must not be null.
     * @throws CommandExecutionException if this operation requires a range and cannot use any sane
     *     default. The message in the exception will be shown to the user.
     */
    public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
            throws CommandExecutionException;
}
