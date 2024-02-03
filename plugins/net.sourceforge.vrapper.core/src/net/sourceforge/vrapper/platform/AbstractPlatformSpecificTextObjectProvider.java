package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

/**
 * Implementers of {@link PlatformSpecificTextObjectProvider} are recommended to extend this class
 * instead. This class is reserved for future API compatibility, preventing breaking changes.
 */
public abstract class AbstractPlatformSpecificTextObjectProvider implements
        PlatformSpecificTextObjectProvider {

    public State<DelimitedText> delimitedTexts() {
        return EmptyState.getInstance();
    }
}
