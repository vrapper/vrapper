package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/** Interface describing delimited piece of text and it's delimiters.
 * 
 * Examples: strings, expression in parenthesis, XML tag.
 * 
 * @author Krzysiek Goj
 */
public interface DelimitedText {
    TextRange leftDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;
    TextRange rightDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;
}
