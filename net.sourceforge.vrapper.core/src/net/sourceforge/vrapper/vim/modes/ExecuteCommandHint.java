package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.commands.Command;

public class ExecuteCommandHint implements ModeSwitchHint {
    
    private final Command command;

    public ExecuteCommandHint(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

}
