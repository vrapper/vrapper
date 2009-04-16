package net.sourceforge.vrapper.keymap;

public class SimpleKeyBinding<T> implements KeyBinding<T> {

	private Transition<T> transition;
	private KeyStroke key;

	public SimpleKeyBinding(KeyStroke key, Transition<T> transition) {
		this.key = key;
		this.transition = transition;
	}

	@Override
	public KeyStroke getKeyPress() {
		return key;
	}

	@Override
	public Transition<T> getTransition() {
		return transition;
	}

}
