package net.sourceforge.vrapper.keymap.vim;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.utils.StringUtils;

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

    public int getModifiers() {
        return modifiers;
    }

    public char getCharacter() {
        return character;
    }

    public SpecialKey getSpecialKey() {
        return specialKey;
    }

    @Override
    public int hashCode() {
        return modifiers ^ character << 16 ^ (specialKey == null ? 0 : specialKey.hashCode());
    }

    @Override
    public String toString() {
        // this is mainly for debugging
        List<String> pieces = new ArrayList<String>();
        if ((modifiers & SHIFT) != 0)
            pieces.add("SHIFT");
        if ((modifiers & ALT) != 0)
            pieces.add("ALT");
        if ((modifiers & CTRL) != 0)
            pieces.add("CTRL");
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
        if (modifiers != other.getModifiers()) {
            return false;
        }
        return true;
    }

}
