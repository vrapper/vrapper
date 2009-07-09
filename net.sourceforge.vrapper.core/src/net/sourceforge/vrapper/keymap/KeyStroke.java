package net.sourceforge.vrapper.keymap;

public interface KeyStroke {
	public static final char SPECIAL_KEY = 0;
	public static final int SHIFT = 0x1;
	public static final int   ALT = 0x2;
	public static final int  CTRL = 0x4;

	int getModifiers();
	char getCharacter();
	SpecialKey getSpecialKey();
	boolean isVirtual();
}
