package net.sourceforge.vrapper.keymap.vim;

import java.util.EnumSet;
import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;

public class SimpleKeyStroke implements KeyStroke {

    private final char character;
    private final SpecialKey specialKey;
    private final Set<Modifier> modifiers;
    
    public SimpleKeyStroke(char character, Set<Modifier> modifiers) {
        this.character = character;
        this.specialKey = null;
        this.modifiers = modifiers;
        if (modifiers == null) {
            throw new NullPointerException("Modifier Set cannot be null");
        }
    }

    public SimpleKeyStroke(SpecialKey key, Set<Modifier> modifiers) {
        this.character = SPECIAL_KEY;
        this.specialKey = key;
        this.modifiers = modifiers;
        if (modifiers == null) {
            throw new NullPointerException("Modifier Set cannot be null");
        }
    }

    public SimpleKeyStroke(char character) {
        this.character = character;
        this.specialKey = null;
        this.modifiers = EnumSet.noneOf(Modifier.class);
    }

    public SimpleKeyStroke(SpecialKey key) {
        this.character = '\0';
        this.specialKey = key;
        this.modifiers = EnumSet.noneOf(Modifier.class);
    }

    /**
     * Copy character or specialkey from source but use different modifiers.
     */
    public SimpleKeyStroke(KeyStroke source, EnumSet<Modifier> modifiers) {
        if (source.getSpecialKey() == null) {
            this.specialKey = null;
            this.character = source.getCharacter();
        } else {
            this.character = SPECIAL_KEY;
            this.specialKey = source.getSpecialKey();
        }
        this.modifiers = modifiers;
        if (modifiers == null) {
            throw new NullPointerException("Modifier Set cannot be null");
        }
    }

    public char getCharacter() {
        return character;
    }

    public SpecialKey getSpecialKey() {
        return specialKey;
    }
    
    public boolean withShiftKey() {
        return modifiers.contains(Modifier.SHIFT);
    }
    
    public boolean withAltKey() {
        return modifiers.contains(Modifier.ALT);
    }

    public boolean withCtrlKey() {
        return modifiers.contains(Modifier.CONTROL);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + character;
        result = prime * result + ((specialKey == null) ? 0 : specialKey.hashCode());
        result = prime * result + (Boolean.valueOf(withCtrlKey()).hashCode());
        result = prime * result + (Boolean.valueOf(withAltKey()).hashCode());
        // No comparisons for Shift or Command, those are rare enough that we call equals some more
        return result;
    }

    @Override
    public String toString() {
        // this is mainly for debugging
        String key = specialKey == null ? Character.toString(character) : specialKey.toString();
        EnumSet<Modifier> modifiers = EnumSet.copyOf(this.modifiers);
        modifiers.remove(Modifier.SHIFT);
        if ((specialKey != null || character == ' ') && withShiftKey()) {
            key = "S-" + key;
        }
        for (Modifier modifier : modifiers) {
            key = modifier.getShortId() + key;
        }
        return "Key(" + key + ")";
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
        EnumSet<Modifier> modifiers = EnumSet.copyOf(this.modifiers);
        modifiers.remove(Modifier.SHIFT);
        EnumSet<Modifier> otherModifiers = EnumSet.copyOf(other.getModifiers());
        otherModifiers.remove(Modifier.SHIFT);

        if (character != other.getCharacter()) {
            return false;
        }
        if (specialKey != other.getSpecialKey()) {
            return false;
        }
        if ( ! modifiers.equals(otherModifiers)) {
            return false;
        }

        //only check shift key if it doesn't change the keycode
        //Shift-space is ok, shift-! is ambiguous with shift-1.
        if ((specialKey != null || character == ' ')
                && withShiftKey() != other.withShiftKey()) {
            return false;
        }

        return true;
    }

    public boolean isVirtual() {
        return false;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }
}
