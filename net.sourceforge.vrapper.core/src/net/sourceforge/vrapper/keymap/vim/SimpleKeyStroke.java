package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;

public class SimpleKeyStroke implements KeyStroke {

	private final int modifiers;
	private final char character;
	private final SpecialKey specialKey;

	public SimpleKeyStroke(int modifiers, char character) {
		this.modifiers = modifiers;
		this.character = character;
		this.specialKey = null;
	}

	public SimpleKeyStroke(int modifiers, SpecialKey key) {
		this.modifiers = modifiers;
		this.character = '\0';
		this.specialKey = key;
	}

	@Override
	public int getModifiers() {
		return modifiers;
	}

	@Override
	public char getCharacter() {
		return character;
	}

	@Override
	public SpecialKey getSpecialKey() {
		return specialKey;
	}

	@Override
	public int hashCode() {
		return modifiers ^ (character << 16) ^ (specialKey == null ? 0 : specialKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof KeyStroke))
			return false;
		KeyStroke other = (KeyStroke) obj;
		if (character != other.getCharacter())
			return false;
		if (specialKey != other.getSpecialKey())
			return false;
		if (modifiers != other.getModifiers())
			return false;
		return true;
	}

}
