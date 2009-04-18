package net.sourceforge.vrapper.vim.commands;


public abstract class CountIgnoringNonRepeatableCommand implements Command {
    public Command repetition() {
        return null;
    }

    public Command withCount(int count) {
        return this;
    }

    public int getCount() {
        return NO_COUNT_GIVEN;
    }

}
