package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.SequenceState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.surround.commands.AddDelimiterCommand;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class AddDelimiterState extends SequenceState<Command, TextObject, DelimiterHolder> {

    public AddDelimiterState(State<TextObject> textObjects, State<DelimiterHolder> delimiters) {
        super(textObjects, delimiters);
    }

    @Override
    protected AddDelimiterState rewrap(State<TextObject> wrapped) {
        return new AddDelimiterState(wrapped, super.second);
    }

    @Override
    protected Function<Command, DelimiterHolder> getConverter(final TextObject textObject) {
        return new Function<Command, DelimiterHolder>() {
                public Command call(DelimiterHolder arg) {
                    return new AddDelimiterCommand(textObject, arg);
                }
        };
    }
    
}
