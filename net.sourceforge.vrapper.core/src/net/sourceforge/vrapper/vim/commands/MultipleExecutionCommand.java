package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class MultipleExecutionCommand implements Command {

    private final int count;
    private final Command command;

    public MultipleExecutionCommand(int count, Command command) {
        this.count = count;
        this.command = command;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        HistoryService history = editorAdaptor.getHistory();
        try {
            history.beginCompoundChange();
            history.lock();
            for (int i = 0; i < count; i++) {
                command.execute(editorAdaptor);
            }
        } finally {
            history.unlock();
            history.endCompoundChange();
        }
    }

    public Command repetition() {
        Command repetition = command.repetition();
        if (repetition != null) {
            return new MultipleExecutionCommand(count, repetition);
        }
        return null;
    }

    public int getCount() {
        return count;
    }

    public Command withCount(int count) {
        return new MultipleExecutionCommand(count, command);
    }

}
