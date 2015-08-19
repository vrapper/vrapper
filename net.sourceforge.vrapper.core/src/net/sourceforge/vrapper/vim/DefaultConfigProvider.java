package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.platform.Configuration.Option;

public interface DefaultConfigProvider {
    /**
     * Checks and returns any runtime defaults for a given option.
     * This method is never called if the given option is explicitly set in a Configuration.
     * @return either <code>null</code> if the Option is not recognized by this provider or an
     *   appropriate value if a suitable default is found.
     */
    public <T> T getDefault(Option<T> option); 
}
