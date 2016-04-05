package net.sourceforge.vrapper.keymap;

import java.util.Set;

public interface KeyStroke {
	public static final char SPECIAL_KEY = 0;
	public static enum Modifier {
		CONTROL("C-"), ALT("A-"), SHIFT("S-");

		private String shortId;

		Modifier(String shortId) {
			this.shortId = shortId;
		}

		public String getShortId() {
			return shortId;
		}
	}

	char getCharacter();
	SpecialKey getSpecialKey();
	boolean isVirtual();
	boolean withShiftKey();
	boolean withAltKey();
	boolean withCtrlKey();
	Set<Modifier> getModifiers();
}
