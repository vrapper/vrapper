package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.plugin.surround.commands.AddDelimiterToSelectionOperation;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperationCommand;
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
            TextOperation operation =
                    new AddDelimiterToSelectionOperation(delimiters, indentOperation);

            if (delimiters instanceof AbstractDynamicDelimiterHolder) {
                //Command will switch to ReplaceDelimiterMode, don't go back to Normal mode now. 
                return new SelectionBasedTextOperationCommand.DontChangeMode(operation);
            } else {
                return new SelectionBasedTextOperationCommand(operation);
            }
        }
        
    }
}
