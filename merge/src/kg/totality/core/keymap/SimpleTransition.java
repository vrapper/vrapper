package kg.totality.core.keymap;


public class SimpleTransition<T> implements Transition<T> {

	private State<T> nextState;
	private T value;

	public SimpleTransition(T value, State<T> nextState) {
		this.value = value;
		this.nextState = nextState;
	}

	public SimpleTransition(T value) {
		this.value = value;
	}

	public SimpleTransition(State<T> nextState) {
		this.nextState = nextState;
	}

	@Override
	public State<T> getNextState() {
		return nextState;
	}

	@Override
	public T getValue() {
		return value;
	}

}
