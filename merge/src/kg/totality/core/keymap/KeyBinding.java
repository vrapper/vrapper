package kg.totality.core.keymap;

public interface KeyBinding<T> {
	KeyStroke getKeyPress();
	Transition<T> getTransition();
}
