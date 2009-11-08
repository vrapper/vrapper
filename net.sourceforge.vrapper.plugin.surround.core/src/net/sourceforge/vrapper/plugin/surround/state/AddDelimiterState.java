package net.sourceforge.vrapper.plugin.surround.state;

import static net.sourceforge.vrapper.plugin.surround.state.DelimiterStrings.DELIMITER_STRINGS;
import net.sourceforge.vrapper.keymap.SequenceState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.surround.commands.AddDelimiterCommand;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class AddDelimiterState extends SequenceState<Command, TextObject, String> {

    public AddDelimiterState(State<TextObject> textObjects) {
        super(textObjects, DELIMITER_STRINGS);
    }

    @Override
    protected AddDelimiterState rewrap(State<TextObject> wrapped) {
        return new AddDelimiterState(wrapped);
    }

    @Override
    protected Function<Command, String> getConverter(final TextObject textObject) {
        return new Function<Command, String>() {
                public Command call(String arg) {
                    String[] split = arg.split("\0");
                    return new AddDelimiterCommand(textObject, split[0], split[1]);
                }
        };
    }
    
}
