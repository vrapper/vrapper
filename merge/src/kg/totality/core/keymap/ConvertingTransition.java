package kg.totality.core.keymap;

import kg.totality.core.utils.Function;

public class ConvertingTransition<T1, T2> implements Transition<T1> {

	private Function<T1, T2> converter;
	private T2 value;
	private State<T2> nextState;

	public ConvertingTransition(Function<T1, T2> converter, T2 value, State<T2> nextState) {
		this.converter = converter;
		this.value = value;
		this.nextState = nextState;
	}

	@Override
	public State<T1> getNextState() {
		if (nextState != null)
			return new ConvertingState<T1, T2>(converter, nextState);
		else return null;
	}

	@Override
	public T1 getValue() {
		if (value != null)
			return converter.call(value);
		return null;
	}

}
