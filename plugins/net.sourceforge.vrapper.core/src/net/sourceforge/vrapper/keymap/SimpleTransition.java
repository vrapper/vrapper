package net.sourceforge.vrapper.keymap;


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

    public State<T> getNextState() {
        return nextState;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("SimpleTransition(%s -> %s)", value, nextState);
    }
}
