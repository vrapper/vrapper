package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Counted;

public class ApplyCount<T> implements Function<T, Counted<T>> {

	private final int count;

	public ApplyCount(int count) {
		this.count = count;
	}

	@Override
	public T call(Counted<T> arg) {
		return arg.withCount(count);
	}

}
