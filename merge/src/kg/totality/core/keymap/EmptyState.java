package kg.totality.core.keymap;

import java.util.ArrayList;

public class EmptyState<T> implements State<T> {

	@Override
	public Transition<T> press(KeyStroke key) {
		return null;
	}

	@Override
	public Iterable<KeyStroke> supportedKeys() {
		return new ArrayList<KeyStroke>();
	}

	@Override
	public State<T> union(State<T> other) {
		return other;
	}

}
