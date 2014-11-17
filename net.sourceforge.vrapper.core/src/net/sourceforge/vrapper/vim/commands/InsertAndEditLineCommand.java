package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.repeat;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.WithCountHint;
import net.sourceforge.vrapper.vim.register.Register;

class RepeatInsertAndEditLineCommand extends CountAwareCommand {

    private Command firstLineInsertion;

    public RepeatInsertAndEditLineCommand(Command firstLineInsertion) {
        this.firstLineInsertion = firstLineInsertion;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        Register lastEditRegister = editorAdaptor.getRegisterManager().getLastEditRegister();
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        InsertLineCommand insertLineCommand;
        InsertMode.createRepetition(lastEditRegister, firstLineInsertion, count, 0, 0)
                    .execute(editorAdaptor);
        if (count > 1) {
            insertLineCommand = InsertLineCommand.POST_CURSOR;
            repeat(count - 1,
                    InsertMode.createRepetition(lastEditRegister, insertLineCommand, count, 0, 0))
                .execute(editorAdaptor);
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }
    
}

public class InsertAndEditLineCommand extends CountAwareCommand {

    public static final Command PRE_CURSOR = new InsertAndEditLineCommand(InsertLineCommand.PRE_CURSOR);
    public static final Command POST_CURSOR = new InsertAndEditLineCommand(InsertLineCommand.POST_CURSOR);
    
    protected Command insertLine;

    public InsertAndEditLineCommand(Command insertLine) {
        this.insertLine = insertLine;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        editorAdaptor.changeModeSafely(InsertMode.NAME, new WithCountHint(count),
                new ExecuteCommandHint.OnEnter(insertLine),
                new ExecuteCommandHint.OnRepeat(InsertLineCommand.POST_CURSOR));
    }

    @Override
    public CountAwareCommand repetition() {
        return new RepeatInsertAndEditLineCommand(insertLine);
    }
}
