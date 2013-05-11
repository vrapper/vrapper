package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class AddDelimiterCommand extends ChangeDelimiterCommand {
    public AddDelimiterCommand(TextObject textObject, DelimiterHolder delimiters) {
        super(new NotYetDelimitedTextObject(textObject), delimiters);
    }
}
