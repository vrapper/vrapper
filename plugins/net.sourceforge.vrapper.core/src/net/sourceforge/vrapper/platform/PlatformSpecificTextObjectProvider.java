package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.TextObjectProvider;

/**
 * Provides text objects for platform specific text editing.
 * @see TextObjectProvider
 */
public interface PlatformSpecificTextObjectProvider extends TextObjectProvider {

    /**
     * @return name of this PlatformSpecificStateProvider (for caching purposes)
     */
    String getName();

}
