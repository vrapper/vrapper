package net.sourceforge.vrapper.plugin.surround.state;

import static net.sourceforge.vrapper.plugin.surround.state.DelimiterStrings.DELIMITER_STRINGS;
import net.sourceforge.vrapper.keymap.SequenceState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.surround.commands.ChangeDelimiterCommand;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

public class ChangeDelimiterState extends SequenceState<Command, DelimitedText, String> {

    public ChangeDelimiterState(State<DelimitedText> wrapped) {
        super(wrapped, DELIMITER_STRINGS);
    }

    @Override
    protected ChangeDelimiterState rewrap(State<DelimitedText> wrapped) {
        return new ChangeDelimiterState(wrapped);
    }

    @Override
    protected Function<Command, String> getConverter(final DelimitedText delimitedText) {
        return new Function<Command, String>() {
                public Command call(String arg) {
                    String[] split = arg.split("\0");
                    return new ChangeDelimiterCommand(delimitedText, split[0], split[1]);
                }
        };
    }
}
