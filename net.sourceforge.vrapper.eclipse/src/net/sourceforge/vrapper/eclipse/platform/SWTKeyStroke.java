package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public class SWTKeyStroke implements KeyStroke {

    private org.eclipse.jface.bindings.keys.KeyStroke keyStroke;

    public SWTKeyStroke(String description) {
        try {
            keyStroke = org.eclipse.jface.bindings.keys.KeyStroke.getInstance(description);
        } catch (ParseException exception) {
            VrapperLog.error("Failed to parse '" + description + "' key description.", exception);
        }
    }

    public SWTKeyStroke(KeyEvent event) {
        int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
        keyStroke = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
    }

    @Override
    public int hashCode() {
        return keyStroke == null ? 0 : keyStroke.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SWTKeyStroke other = (SWTKeyStroke) obj;
        return keyStroke != null && other.keyStroke != null && keyStroke.equals(other.keyStroke);
    }

    @Override
    public String toString() {
        return keyStroke.toString();
    }

    public boolean isComplete() {
        return keyStroke.isComplete();
    }

    public char getCharacter() {
        char chr = (char) keyStroke.getNaturalKey();
        if (Character.isLetter(chr) && (keyStroke.getModifierKeys() & SWT.SHIFT) == 0) {
            chr = Character.toLowerCase(chr);
        }
        return chr;
    }

    public int getModifiers() {
        int modifiers = keyStroke.getModifierKeys();
        int result = 0;
        if ((modifiers & SWT.CTRL) != 0) {
            result |= CTRL;
        }
        if ((modifiers & SWT.ALT) != 0) {
            result |= ALT;
        }
        if ((modifiers & SWT.SHIFT) != 0) {
            result |= SHIFT;
        }
        return result;
    }

    public SpecialKey getSpecialKey() {
        return null; // FIXME: it's broken
    }

    public boolean isVirtual() {
        return false;
    }

}
