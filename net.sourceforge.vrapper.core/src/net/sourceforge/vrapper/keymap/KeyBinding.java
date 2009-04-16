package net.sourceforge.vrapper.keymap;

public interface KeyBinding<T> {
	KeyStroke getKeyPress();
	Transition<T> getTransition();
}
