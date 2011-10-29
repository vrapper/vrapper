package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.commands.Command;

public abstract class ExecuteCommandHint implements ModeSwitchHint {

    private final Command command;

    public ExecuteCommandHint(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
    
    public static class OnEnter extends ExecuteCommandHint {
		public OnEnter(Command command) { super(command); }
    }

    public static class OnLeave extends ExecuteCommandHint {
		public OnLeave(Command command) { super(command); }
    }

}
