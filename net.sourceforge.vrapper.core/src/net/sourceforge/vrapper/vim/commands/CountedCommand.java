package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class CountedCommand implements Command {

    private final int count;
    protected final CountAwareCommand command;

    public CountedCommand(int count, CountAwareCommand command) {
        this.count = count;
        this.command = command;
    }

    public void execute(EditorAdaptor editorAdaptor) {
        command.execute(editorAdaptor, count);
    }

    public Command repetition() {
        CountAwareCommand repetition = command.repetition();
        if (repetition != null) {
            return new CountedCommand(count, repetition);
        }
        return repetition;
    }

    public int getCount() {
        return count;
    }

    public Command withCount(int count) {
        return new CountedCommand(count, command);
    }

}
