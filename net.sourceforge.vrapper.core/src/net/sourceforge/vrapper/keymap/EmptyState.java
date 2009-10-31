package net.sourceforge.vrapper.keymap;

public class EmptyState<T> implements State<T> {
    
    private static EmptyState<?> INSTANCE = new EmptyState<Object>();
    
    private EmptyState() { /* NOP */ }

    public Transition<T> press(KeyStroke key) {
        return null;
    }

	public State<T> union(State<T> other) {
        return other;
    }

    @SuppressWarnings("unchecked")
    public static<T> State<T> getInstance() {
        return (State<T>) INSTANCE;
    }

}
