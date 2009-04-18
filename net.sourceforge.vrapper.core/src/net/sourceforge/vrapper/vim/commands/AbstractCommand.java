package net.sourceforge.vrapper.vim.commands;


public abstract class AbstractCommand implements Command {
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

}
