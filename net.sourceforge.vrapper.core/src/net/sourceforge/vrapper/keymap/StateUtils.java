package net.sourceforge.vrapper.keymap;



public class StateUtils {
	public static<T> T firstNonNull(T a, T b) {
		return a != null ? a : b;
	}

    @SafeVarargs
	public static<T> State<T> union(State<T>... states) {
		State<T> result = EmptyState.getInstance();
		for (State<T> state: states)
			if (state != null)
				result = result.union(state);
		return result;
	}

    @SafeVarargs
	public static<T> State<T> unionOrNull(State<T>... states) {
		State<T> result = union(states);
		if (result instanceof EmptyState<?>)
			return result;
		return null;
	}

	public static<T> Transition<T> transitionUnion(Transition<T> t1, Transition<T> t2) {
	    if (t1 == null || t2 == null)
	        return firstNonNull(t1, t2);
		T value = firstNonNull(t1.getValue(), t2.getValue());
		State<T> state;
		State<T> t1ns = t1.getNextState();
		State<T> t2ns = t2.getNextState();
		if (t1ns != null && t2ns != null)
			state = t1.getNextState().union(t2.getNextState());
		else
		    state = firstNonNull(t1.getNextState(), t2.getNextState());
		return new SimpleTransition<T>(value, state);
	}
}
