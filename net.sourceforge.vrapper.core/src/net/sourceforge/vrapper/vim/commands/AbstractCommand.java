package net.sourceforge.vrapper.vim.commands;


public abstract class AbstractCommand implements Command {
	@Override
	public int getCount() {
		return NO_COUNT_GIVEN;
	}

}
