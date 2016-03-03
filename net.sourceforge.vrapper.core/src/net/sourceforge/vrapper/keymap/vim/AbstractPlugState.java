package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.RemappedKeyStroke;

/**
 * This abstract state detects <code>Plug</code> keystrokes and extracts their id for handling.
 */
public abstract class AbstractPlugState<T> implements State<T> {

    @Override
    public Transition<T> press(KeyStroke key) {
        if (SpecialKey.PLUG == key.getSpecialKey()) {
            PlugKeyStroke plug;
            if (key instanceof RemappedKeyStroke) {
                key = (PlugKeyStroke) ((RemappedKeyStroke)key).unwrap();
            }
            if (key instanceof PlugKeyStroke) {
                plug = (PlugKeyStroke) key;
            } else {
                throw new VrapperPlatformException("Bad usage of PLUG key found, key is instance "
                        + "of " + key.getClass().getName() + " whereas "
                        + PlugKeyStroke.class.getName() + " was expected.");
            }
            if (plug.getId() == null || plug.getId().trim().length() == 0) {
                throw new VrapperPlatformException("Bad usage of PLUG key found, id is null, empty"
                        + " or solely whitespace.");
            } else {
                return press(plug.getId());
            }
        }
        return null;
    }

    /**
     * Override this method to handle plugs. The id is guaranteed to be more than whitespace.
     * @return a {@link Transition} if the plug key was mapped or <code>null</code> if the id is
     *     not recognized.
     */
    public abstract Transition<T> press(String id);
}
