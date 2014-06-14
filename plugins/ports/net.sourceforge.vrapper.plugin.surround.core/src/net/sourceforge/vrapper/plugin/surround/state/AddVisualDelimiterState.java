package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.surround.commands.AddDelimiterToSelectionCommand;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextOperation;

public class AddVisualDelimiterState extends ConvertingState<Command, DelimiterHolder> {

    /**
     * @param isGMode Whether this command was invoked as 'vS' or as 'vgS'.
     * @param indentOperation a {@link TextOperation} instance which is applied in case the
     *     selection is line-wise. Can be null for no indentation.
     */
    public AddVisualDelimiterState(boolean isGMode, TextOperation indentOperation,
            State<DelimiterHolder> delimiters) {
        super(new Converter(isGMode, indentOperation), delimiters);
    }
    

    protected static class Converter implements Function<Command, DelimiterHolder> {

        private TextOperation indentOperation;
        private boolean isGMode;

        public Converter(boolean isGMode, TextOperation indentOperation) {
            this.indentOperation = indentOperation;
            this.isGMode = isGMode;
        }

        @Override
        public Command call(DelimiterHolder delimiters) {
            return new AddDelimiterToSelectionCommand(delimiters, isGMode, indentOperation);
        }
        
    }
}
