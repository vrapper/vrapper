package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.repeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class RepeatInsertionCommand extends CountAwareCommand {

    private final Command command;

    public RepeatInsertionCommand(Command command) {
        this.command = command;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        Command lastInsertion = editorAdaptor.getRegisterManager().getLastInsertion();
        Command doIt = seq(command, lastInsertion);
        if (count == NO_COUNT_GIVEN)
            count = lastInsertion.getCount();
        if (count != NO_COUNT_GIVEN)
            doIt = repeat(count, doIt);
        doIt.execute(editorAdaptor);
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }
}