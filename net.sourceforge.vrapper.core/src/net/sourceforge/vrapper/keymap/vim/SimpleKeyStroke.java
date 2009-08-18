package net.sourceforge.vrapper.keymap.vim;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.utils.StringUtils;

public class SimpleKeyStroke implements KeyStroke {

    private final char character;
    private final SpecialKey specialKey;

    public SimpleKeyStroke(char character) {
        this.character = character;
        this.specialKey = null;
    }

    public SimpleKeyStroke(SpecialKey key) {
        this.character = '\0';
        this.specialKey = key;
    }

    public char getCharacter() {
        return character;
    }

    public SpecialKey getSpecialKey() {
        return specialKey;
    }

    @Override
    public int hashCode() {
        return character << 16 ^ (specialKey == null ? 0 : specialKey.hashCode());
    }

    @Override
    public String toString() {
        // this is mainly for debugging
        List<String> pieces = new ArrayList<String>();
        pieces.add(specialKey == null ? Character.toString(character) : specialKey.toString());
        return "SimpleKeyStroke(" + StringUtils.join("+", pieces) + ")";
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
