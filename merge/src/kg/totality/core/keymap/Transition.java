package kg.totality.core.keymap;

public interface Transition<T> {

	T getValue();
	State<T> getNextState();

}
