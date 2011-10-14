package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.commands.Command;

public class ExecuteCommandOnCompleteHint implements ModeSwitchHint {
    
    private final Command command;

    public ExecuteCommandOnCompleteHint(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
