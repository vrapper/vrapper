package net.sourceforge.vrapper.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.transitionUnion;

public class UnionState<T> implements State<T> {

    protected final State<T> state1;
    protected final State<T> state2;

    @SuppressWarnings("unchecked")
	public UnionState(State<? extends T> state1, State<? extends T> state2) {
        this.state1 = (State<T>) state1;
        this.state2 = (State<T>) state2;
    }

    public Transition<T> press(KeyStroke key) {
        return transitionUnion(state1.press(key), state2.press(key));
    }

    public State<T> union(State<T> other) {
        return new UnionState<T>(this, other);
    }

}
