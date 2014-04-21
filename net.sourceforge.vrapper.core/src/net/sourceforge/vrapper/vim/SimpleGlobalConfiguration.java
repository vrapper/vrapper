package net.sourceforge.vrapper.vim;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.vrapper.platform.GlobalConfiguration;
import net.sourceforge.vrapper.platform.SimpleConfiguration;

/**
 * Configuration implementation used for holding the global settings which are used as defaults in
 * new editors.
 */
// This class is implemented in core as it doesn't really depend on platform-specific code.
public class SimpleGlobalConfiguration extends SimpleConfiguration implements GlobalConfiguration {

    protected List<ConfigurationListener> listeners =
            new CopyOnWriteArrayList<ConfigurationListener>();

    @Override
    public <T> void set(Option<T> key, T value) {
        T oldValue = super.get(key);
        super.set(key, value);
        for (ConfigurationListener listener : listeners) {
            listener.optionChanged(key, oldValue, value);
        }
    }

    public void addListener(ConfigurationListener listener) {
        listeners.add(listener);
    }
 
    public void removeListener(ConfigurationListener listener) {
        listeners.remove(listener);
    }

}
