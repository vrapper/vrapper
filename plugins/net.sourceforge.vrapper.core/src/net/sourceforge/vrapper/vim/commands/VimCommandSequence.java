package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class VimCommandSequence extends SimpleRepeatableCommand {

    private final List<Command> commands = new ArrayList<Command>();

    public VimCommandSequence(Command... commands) {
        for (Command cmd: commands) {
            if (cmd != null)
                this.commands.add(cmd);
        }
    }

    public void execute(EditorAdaptor editorMode) throws CommandExecutionException {
        HistoryService history = editorMode.getHistory();
        try {
            history.beginCompoundChange();
            history.lock();
            for (Command command: commands) {
                command.execute(editorMode);
            }
        } finally {
            history.unlock();
            history.endCompoundChange();
        }
    }

    public Command repetition() {
        Command[] repeated = new Command[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            Command cmd = commands.get(i);
            Command rep = cmd.repetition();
            if (rep != null) {
                repeated[i] = rep;
            } else {
                repeated[i] = cmd;
            }
        }
        return new VimCommandSequence(repeated);
    }
    
    @Override
    public String toString() {
    	return String.format("seq(%s)", StringUtils.join(", ", commands));
    }

}
