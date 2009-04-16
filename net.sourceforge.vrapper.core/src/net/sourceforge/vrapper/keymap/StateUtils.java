package net.sourceforge.vrapper.keymap;



public class StateUtils {
	public static<T> T firstNonNull(T a, T b) {
		return a != null ? a : b;
	}

	public static<T> State<T> union(State<T>... states) {
		State<T> result = new EmptyState<T>();
		for (State<T> state: states)
			if (state != null)
				result = result.union(state);
		return result;
	}

	public static<T> State<T> unionOrNull(State<T>... states) {
		State<T> result = union(states);
		if (result.supportedKeys().iterator().hasNext())
			return result;
		return null;
	}

	public static<T> Transition<T> transitionUnion(Transition<T> t1, Transition<T> t2) {
		T value = firstNonNull(t1.getValue(), t2.getValue());
		State<T> t1ns = t1.getNextState();
		State<T> t2ns = t2.getNextState();
		State<T> state = firstNonNull(t1.getNextState(), t2.getNextState());
		if (t1ns != null && t2ns != null)
			state = t1.getNextState().union(t2.getNextState());
		return new SimpleTransition<T>(value, state);
	}
}
