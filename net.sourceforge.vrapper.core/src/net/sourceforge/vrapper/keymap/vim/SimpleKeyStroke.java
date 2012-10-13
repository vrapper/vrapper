package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;

public class SimpleKeyStroke implements KeyStroke {

    private final char character;
    private final SpecialKey specialKey;
    private final boolean shiftKey;
    
    public SimpleKeyStroke(char character, boolean shiftKey) {
        this.character = character;
        this.specialKey = null;
        this.shiftKey = shiftKey;
    }

    public SimpleKeyStroke(SpecialKey key, boolean shiftKey) {
        this.character = '\0';
        this.specialKey = key;
        this.shiftKey = shiftKey;
    }

    public SimpleKeyStroke(char character) {
        this.character = character;
        this.specialKey = null;
        this.shiftKey = false;
    }

    public SimpleKeyStroke(SpecialKey key) {
        this.character = '\0';
        this.specialKey = key;
        this.shiftKey = false;
    }

    public char getCharacter() {
        return character;
    }

    public SpecialKey getSpecialKey() {
        return specialKey;
    }
    
    public boolean withShiftKey() {
    	return shiftKey;
    }

    @Override
    public int hashCode() {
        return character << 16 ^ (specialKey == null ? 0 : specialKey.hashCode());
    }

    @Override
    public String toString() {
        // this is mainly for debugging
        String key = specialKey == null ? Character.toString(character) : specialKey.toString();
        return "SimpleKeyStroke(" + key + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KeyStroke)) {
            return false;
        }
        KeyStroke other = (KeyStroke) obj;
        if (character != other.getCharacter()) {
            return false;
        }
        if (specialKey != other.getSpecialKey()) {
            return false;
        }
        return true;
    }

    public boolean isVirtual() {
        return false;
    }

}
