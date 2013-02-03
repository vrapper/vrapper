package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;

public class SimpleKeyStroke implements KeyStroke {

    private final char character;
    private final SpecialKey specialKey;
    private final boolean shiftKey;
    private final boolean altKey;
    
    public SimpleKeyStroke(char character, boolean shiftKey, boolean altKey) {
        this.character = character;
        this.specialKey = null;
        this.shiftKey = shiftKey;
        this.altKey = altKey;
    }

    public SimpleKeyStroke(SpecialKey key, boolean shiftKey, boolean altKey) {
        this.character = '\0';
        this.specialKey = key;
        this.shiftKey = shiftKey;
        this.altKey = altKey;
    }

    public SimpleKeyStroke(char character) {
        this.character = character;
        this.specialKey = null;
        this.shiftKey = false;
        this.altKey = false;
    }

    public SimpleKeyStroke(SpecialKey key) {
        this.character = '\0';
        this.specialKey = key;
        this.shiftKey = false;
        this.altKey = false;
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
    
    public boolean withAltKey() {
    	return altKey;
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
        if (altKey != other.withAltKey()) {
            return false;
        }
        //only check shift key if it doesn't change the keycode
        if (specialKey != null && withShiftKey() != other.withShiftKey()) {
        	return false;
        }
        return true;
    }

    public boolean isVirtual() {
        return false;
    }

}
