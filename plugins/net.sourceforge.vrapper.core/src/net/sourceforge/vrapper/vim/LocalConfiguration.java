package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.platform.Configuration;

public interface LocalConfiguration extends Configuration {

    public void removeListener(ConfigurationListener listener);

    public <T> void setLocal(Option<T> key, T value);

    /** Listener which is called if a setting changes in the current editor. */
    public void addListener(ConfigurationListener listener);

    public void setListenersEnabled(boolean enabled);
}
