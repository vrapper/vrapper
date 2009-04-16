package kg.totality.core.keymap.vim;

import kg.totality.core.commands.Counted;
import kg.totality.core.utils.Function;

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
