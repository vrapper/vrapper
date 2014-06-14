package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.SequenceState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.surround.commands.ChangeDelimiterCommand;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

public class ChangeDelimiterState extends SequenceState<Command, DelimitedText, DelimiterHolder> {

    public ChangeDelimiterState(State<DelimitedText> wrapped, State<DelimiterHolder> targetDelimiters) {
        super(wrapped, targetDelimiters);
    }

    @Override
    protected ChangeDelimiterState rewrap(State<DelimitedText> wrapped) {
        return new ChangeDelimiterState(wrapped, super.second);
    }

    @Override
    protected Function<Command, DelimiterHolder> getConverter(final DelimitedText delimitedText) {
        return new Function<Command, DelimiterHolder>() {
                public Command call(DelimiterHolder arg) {
                    return new ChangeDelimiterCommand(delimitedText, arg);
                }
        };
    }
}
