package net.sourceforge.vrapper.keymap;

import net.sourceforge.vrapper.utils.Function;

class CovariantId<T1, T2 extends T1> implements Function<T1, T2> {

	@Override
	public T1 call(T2 arg) {
		return arg;
	}

}

public class CovariantState<T1, T2 extends T1> extends ConvertingState<T1, T2> {

	public CovariantState(State<T2> wrapped) {
		super(new CovariantId<T1, T2>(), wrapped);
	}

}
