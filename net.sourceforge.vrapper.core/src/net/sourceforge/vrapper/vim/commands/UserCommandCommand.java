package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;

/**
 * Execute a user-defined command.  Note that the command could be
 * multiple commands chained together so we need to run the parseAndExecute
 * command directly to make sure it executes exactly as if the user had just
 * typed it.  This means it's a bit of a hack to access that method.
 */
public class UserCommandCommand extends CountIgnoringNonRepeatableCommand {
    
    private String command;
    
    public UserCommandCommand(String command) {
        this.command = command;
    }
    
    public String getCommandString() {
        return command;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        editorAdaptor.changeModeSafely(CommandLineMode.NAME);
        CommandLineMode mode = (CommandLineMode)editorAdaptor.getMode(CommandLineMode.NAME);
        CommandLineParser parser = (CommandLineParser)mode.createParser();
        Command toRun = parser.parseAndExecute(null, command);
        if(toRun != null) {
            toRun.execute(editorAdaptor);
        }
        editorAdaptor.changeModeSafely(NormalMode.NAME);
    }

}
