package net.sourceforge.vrapper.plugin.ipmotion.commands;

import net.sourceforge.vrapper.vim.commands.TextObject;

public class AddDelimiterCommand extends ChangeDelimiterCommand {
    public AddDelimiterCommand(TextObject textObject, String left, String right) {
        super(new NotYetDelimitedTextObject(textObject), left, right);
    }
}
