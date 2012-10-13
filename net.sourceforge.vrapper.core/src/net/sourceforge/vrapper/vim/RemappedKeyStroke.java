package net.sourceforge.vrapper.vim;

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

    public boolean isRecursive() {
        return recursive;
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
