package net.sourceforge.vrapper.plugin.ipmotion.commands;

import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

public class DeleteDelimitersCommand extends ChangeDelimiterCommand {

    public DeleteDelimitersCommand(DelimitedText delimitedText) {
        super(delimitedText, "", "");
    }

    public static final Function<Command, DelimitedText> CONVERTER = new Function<Command, DelimitedText>() {
        public Command call(DelimitedText delimitedText) {
            return new DeleteDelimitersCommand(delimitedText);
        }
    };
    

}