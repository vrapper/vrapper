package net.sourceforge.vrapper.vim.commands;

public class MultiplicableCountedCommand extends CountedCommand {

	public MultiplicableCountedCommand(int count, CountAwareCommand command) {
		super(count, command);
	}

	@Override
	public Command withCount(int count) {
		return new CountedCommand(count * getCount(), command);
	}

}
