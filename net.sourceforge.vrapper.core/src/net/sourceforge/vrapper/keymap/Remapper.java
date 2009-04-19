package net.sourceforge.vrapper.keymap;

public interface Remapper<T>  {
	void addMapping(State<? extends Remapping> mappings);
    State<T> getState();
}
