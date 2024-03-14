package net.sourceforge.vrapper.keymap;


/**
 * Interface describing keymap state at some point.
 * @author Krzysiek Goj
 *
 * @param <T> - What does this state return
 */
public interface State<T> {
	Transition<T> press(KeyStroke key);
	State<T> union(State<T> other);
	// TODO: map() method
}
