package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.plugin.surround.commands.AddDelimiterToSelectionOperation;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;

public class AddVisualDelimiterState extends ConvertingState<Command, DelimiterHolder> {

    /**
     * @param indentOperation a {@link TextOperation} instance which is applied in case the
     *     selection is line-wise. Can be null for no indentation.
     */
    public AddVisualDelimiterState(TextOperation indentOperation) {
        super(new Converter(indentOperation), DelimiterValues.DELIMITER_HOLDERS);
    }
    
    protected static class VisualCommand implements Command {

        final private AddDelimiterToSelectionOperation operation;
        final boolean changeMode;
        final boolean repetitionWrapper;

        public VisualCommand(DelimiterHolder delimiters, TextOperation indentOperation) {
            this.operation = new AddDelimiterToSelectionOperation(delimiters, indentOperation);
            this.changeMode = !(delimiters instanceof AbstractDynamicDelimiterHolder);
            this.repetitionWrapper = false;
        }

        private VisualCommand(AddDelimiterToSelectionOperation operation, boolean changeMode) {
            this.operation = operation;
            this.changeMode = changeMode;
            this.repetitionWrapper = true;
        }

        @Override
        public Command repetition() {
            return new VisualCommand(operation, changeMode);
        }

        @Override
        public Command withCount(int count) {
            return this;
        }

        @Override
        public int getCount() {
            return NO_COUNT_GIVEN;
        }

        @Override
        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            TextObject selection;
            if (repetitionWrapper) {
                selection = editorAdaptor.getLastActiveSelectionArea();
            } else {
                editorAdaptor.rememberLastActiveSelection();
                selection = editorAdaptor.getSelection();
            }
            operation.execute(editorAdaptor, NO_COUNT_GIVEN, selection);
            if (changeMode) {
                LeaveVisualModeCommand.doIt(editorAdaptor);
            }
        }
    };

    protected static class Converter implements Function<Command, DelimiterHolder> {

        private TextOperation indentOperation;

        public Converter(TextOperation indentOperation) {
            this.indentOperation = indentOperation;
        }

        @Override
        public Command call(DelimiterHolder delimiters) {
            return new VisualCommand(delimiters, indentOperation);
        }
        
    }
}
