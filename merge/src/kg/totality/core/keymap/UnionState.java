package kg.totality.core.keymap;

import static kg.totality.core.keymap.StateUtils.firstNonNull;

import java.util.HashSet;
import java.util.Set;


public class UnionState<T> implements State<T> {

	private final State<T> state1;
	private final State<T> state2;

	public UnionState(State<T> state1, State<T> state2) {
		this.state1 = state1;
		this.state2 = state2;
	}

	@Override
	public Transition<T> press(KeyStroke key) {
		Transition<T> transition1 = state1.press(key);
		Transition<T> transition2 = state2.press(key);
		if (transition1 == null || transition2 == null)
			return firstNonNull(transition1, transition2);
		State<T> nextState1 = transition1.getNextState();
		State<T> nextState2 = transition2.getNextState();
		T unionValue = firstNonNull(transition1.getValue(), transition2.getValue());
		@SuppressWarnings("unchecked")
		State<T> unionState = StateUtils.unionOrNull(nextState1, nextState2);
		return new SimpleTransition<T>(unionValue, unionState);
	}

	@Override
	public Iterable<KeyStroke> supportedKeys() {
		Set<KeyStroke> set = new HashSet<KeyStroke>();
		for (KeyStroke stroke: state1.supportedKeys()) set.add(stroke);
		for (KeyStroke stroke: state2.supportedKeys()) set.add(stroke);
		return set;
	}

	@Override
	public State<T> union(State<T> other) {
		return new UnionState<T>(this, other);
	}

}
