package net.sourceforge.vrapper.vim.commands;

import java.util.Map;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

/**
 * List all user-defined commands created with ":command".
 * Note that ":command" by itself lists all user-defined commands
 * and ":command <prefix>" will list all user-defined commands
 * which start with <prefix>.
 */
public class ListUserCommandsCommand extends AbstractMessagesCommand {
    
    private String name;

    public ListUserCommandsCommand() {
        this("");
    }
    
    public ListUserCommandsCommand(String name) {
        this.name = name;
    }

    @Override
    protected String getMessages(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        EvaluatorMapping maps = editorAdaptor.getPlatformSpecificStateProvider().getCommands();
        Map<String, String> userDefined = maps.getUserDefined();

        StringBuilder list = new StringBuilder();
        list.append(" Name        Definition\n");
        for(String command : userDefined.keySet()) {
            if(command.startsWith(name)) {
                list.append(
                   String.format(" %1$-11s %2$s\n", command, userDefined.get(command))
                );
            }
        }

        return list.toString();
    }

}
