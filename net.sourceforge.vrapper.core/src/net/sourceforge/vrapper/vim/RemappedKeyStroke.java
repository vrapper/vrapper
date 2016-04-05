package net.sourceforge.vrapper.vim;

import java.util.EnumSet;
import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;

/**
 * Wrapper class for {@link KeyStroke} which provides an additional
 * recursive property.
 *
 * @author Matthias Radig
 */
public class RemappedKeyStroke implements KeyStroke {

    private final KeyStroke delegate;
    private final boolean recursive;

    public RemappedKeyStroke(KeyStroke delegate, boolean recursive) {
        super();
        this.delegate = delegate;
        this.recursive = recursive;
    }

    public char getCharacter() {
        return delegate.getCharacter();
    }

    public SpecialKey getSpecialKey() {
        return delegate.getSpecialKey();
    }
    
    public boolean withShiftKey() {
        return delegate.withShiftKey();
    }

    public boolean withAltKey() {
        return delegate.withAltKey();
    }

    public boolean withCtrlKey() {
        return delegate.withCtrlKey();
    }

    public boolean isRecursive() {
        return recursive;
    }

    public Set<Modifier> getModifiers() {
        return delegate.getModifiers();
    }

    @Override
    public String toString() {
        String key = getSpecialKey() == null ? Character.toString(getCharacter()) : getSpecialKey().toString();
        EnumSet<Modifier> modifiers = EnumSet.copyOf(getModifiers());
        modifiers.remove(Modifier.SHIFT);
        if ((getSpecialKey() != null || getCharacter() == ' ') && withShiftKey()) {
            key = "S-" + key;
        }
        for (Modifier modifier : modifiers) {
            key = modifier.getShortId() + key;
        }
        return "RemappedKeyStroke(" + key + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isVirtual() {
        return true;
    }
}
