package net.sourceforge.vrapper.platform;

import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * Provides extra {@link EditorMode} implementations in case of interactive plugins.
 * <p>
 * Commands to switch to these new modes should be passed through a
 *  {@link PlatformSpecificStateProvider} implementation.
 *
 * @author Bert Jacobs
 */
public interface PlatformSpecificModeProvider {
    
    /**
     * @return a {@link List} of extended {@link EditorMode}s
     *  or {@link Collections#emptyList()}.
     * 
     * <p>Each mode's name should be unique to prevent clashes when switching modes. Preferably use
     *  the FQCN of the class implementing the mode.
     * <p>The display name of the modes are up to the implementer, though it is advised to use
     *  something different from the standard modes so the user doesn't get confused.
     */
    List<EditorMode> getModes(EditorAdaptor editorAdaptor);
    
    /**
     * @return name of this {@link PlatformSpecificModeProvider} (for debugging purposes)
     */
    String getName();
    
}
