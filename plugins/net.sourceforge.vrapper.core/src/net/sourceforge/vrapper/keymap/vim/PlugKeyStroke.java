package net.sourceforge.vrapper.keymap.vim;

import java.util.EnumSet;
import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;

/**
 * A virtual keystroke which is used as a generic trigger for optional commands or motions. Remaps
 * can then use this for the "target" map. Note that the plug id is case sensitive!
 * <p>Example remap for a user's <code>.vrapperrc</code>:
 * <code>:nnoremap gW &lt;Plug&gt;(vrapper.window.move.up)</code>
 * @see ConstructorWrappers#parseKeyStrokes(String)
 */
public class PlugKeyStroke implements KeyStroke {

    private String plugId;

    public PlugKeyStroke(String plugId) {
        this.plugId = plugId;
        if ( ! plugId.startsWith("(") || ! plugId.endsWith(")")) {
            throw new IllegalArgumentException("Plug id must start and end with parentheses, got "
                    + plugId);
        }
    }

    public String getId() {
        return plugId;
    }

    @Override
    public char getCharacter() {
        return KeyStroke.SPECIAL_KEY;
    }

    @Override
    public SpecialKey getSpecialKey() {
        return SpecialKey.PLUG;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public boolean withShiftKey() {
        return false;
    }

    @Override
    public boolean withAltKey() {
        return false;
    }

    @Override
    public boolean withCtrlKey() {
        return false;
    }

    public Set<Modifier> getModifiers() {
        return EnumSet.noneOf(Modifier.class);
    }

    @Override
    public int hashCode() {
        final int prime = 68281;
        int result = 1;
        result = prime * result + ((plugId == null) ? 0 : plugId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlugKeyStroke other = (PlugKeyStroke) obj;
        if (plugId == null) {
            if (other.plugId != null)
                return false;
        } else if (!plugId.equals(other.plugId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        // PlugId should already contain parentheses.
        return "Plug" + plugId;
    }
}
