package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class CountAwareCommand implements Command {
    /**
     * Executes this command. This may have some side effects.
     * @param editorAdaptor adaptor of editor this command was executed on
     * @throws CommandExecutionException
     */
    public abstract void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;

    public abstract CountAwareCommand repetition();

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        execute(editorAdaptor, NO_COUNT_GIVEN);
    }

    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    public Command withCount(int count) {
        return new CountedCommand(count, this);
    }

}
