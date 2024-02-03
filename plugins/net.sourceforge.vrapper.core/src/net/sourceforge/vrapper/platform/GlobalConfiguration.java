package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.ConfigurationListener;

/**
 * Interface for the global configuration.
 */
public interface GlobalConfiguration extends Configuration {

    public void removeListener(ConfigurationListener listener);

    /** Add a listener which gets called if a setting is set globally. */
    public void addListener(ConfigurationListener listener);
}
