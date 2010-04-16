package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.commands.Command;

public class ExecuteCommandOnEnterHint implements ModeSwitchHint {

    private final Command command;

    public ExecuteCommandOnEnterHint(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

}
