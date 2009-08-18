package net.sourceforge.vrapper.keymap;

public interface KeyStroke {
	public static final char SPECIAL_KEY = 0;

	char getCharacter();
	SpecialKey getSpecialKey();
	boolean isVirtual();
}
