package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

/**
 * Provides a number of text objects.
 */
public interface TextObjectProvider {

    public State<DelimitedText> delimitedTexts();

    public State<TextObject> textObjects();
}
