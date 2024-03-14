package net.sourceforge.vrapper.keymap;

public interface Transition<T> {

	T getValue();
	State<T> getNextState();

}
