package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.plugin.surround.commands.AddDelimiterToSelectionOperation;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextOperation;

public class AddVisualDelimiterState extends ConvertingState<Command, DelimiterHolder> {

    /**
     * @param indentOperation a {@link TextOperation} instance which is applied in case the
     *     selection is line-wise. Can be null for no indentation.
     */
    public AddVisualDelimiterState(TextOperation indentOperation) {
        super(new Converter(indentOperation), DelimiterValues.DELIMITER_HOLDERS);
    }
    

    protected static class Converter implements Function<Command, DelimiterHolder> {

        private TextOperation indentOperation;

        public Converter(TextOperation indentOperation) {
            this.indentOperation = indentOperation;
        }

        @Override
        public Command call(DelimiterHolder delimiters) {
            return new AddDelimiterToSelectionOperation(delimiters, indentOperation);
        }
        
    }
}
