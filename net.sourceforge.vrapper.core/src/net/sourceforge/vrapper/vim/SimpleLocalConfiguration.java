package net.sourceforge.vrapper.vim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SimpleConfiguration;

/** Wraps a {@link Configuration}, allowing to notify {@link ConfigurationListener}. */
public class SimpleLocalConfiguration extends SimpleConfiguration implements LocalConfiguration {

    protected Configuration sharedConfiguration;

    protected List<ConfigurationListener> listeners =
            new CopyOnWriteArrayList<ConfigurationListener>();
    private boolean listenersEnabled;

    public SimpleLocalConfiguration(List<DefaultConfigProvider> defaultConfigProviders,
            Configuration sharedConfiguration) {
        super(hookSharedConfigurationProviders(defaultConfigProviders, sharedConfiguration));
        this.sharedConfiguration = sharedConfiguration;
        //Don't share the newline.  Each file has its own newline.
        //(in case you have one windows file open
        //  and one unix file open at the same time)
        setNewLine(sharedConfiguration.getNewLine());
    }
    
    protected final static List<DefaultConfigProvider> hookSharedConfigurationProviders(
            List<DefaultConfigProvider> providers, Configuration sharedConfiguration) {
        List<DefaultConfigProvider> list = new ArrayList<DefaultConfigProvider>(providers);
        list.add(0, new SharedConfigurationValueProvider(sharedConfiguration));
        list.add(new SharedConfigurationDefaultProvider(sharedConfiguration));
        return list;
    }

    public <T> void set(Option<T> key, T value) {
        T oldValue = sharedConfiguration.get(key);
        if ( ! key.getScope().equals(OptionScope.LOCAL)) {
            sharedConfiguration.set(key, value);
        }
        if (isSet(key) || key.getScope().equals(OptionScope.LOCAL)) {
            oldValue = get(key);
            super.set(key, value);
        }
        if (listenersEnabled) {
            for (ConfigurationListener listener : listeners) {
                listener.optionChanged(key, oldValue, value);
            }
        }
    }

    @Override
    public <T> void setLocal(Option<T> key, T value) {
        if (key.getScope() == OptionScope.GLOBAL) {
            set(key, value);
        } else {
            T oldValue = get(key);
            if (key.getScope().equals(OptionScope.DEFAULT) && oldValue == null) {
                oldValue = sharedConfiguration.get(key);
            }
            super.set(key, value);
            if (listenersEnabled) {
                for (ConfigurationListener listener : listeners) {
                    listener.optionChanged(key, oldValue, value);
                }
            }
        }
    }
    
    public void setListenersEnabled(boolean enabled) {
        listenersEnabled = enabled;
    }
    
    public void addListener(ConfigurationListener listener) {
        listeners.add(listener);
    }
 
    public void removeListener(ConfigurationListener listener) {
        listeners.remove(listener);
    }

    /**
     * If an option is not set locally, the first "default" place to look is in the shared config.
     * This provider is meant to be the first default config provider.
     */
    static class SharedConfigurationValueProvider implements DefaultConfigProvider {

        private Configuration sharedConfiguration;

        public SharedConfigurationValueProvider(Configuration sharedConfiguration) {
            this.sharedConfiguration = sharedConfiguration;
        }

        @Override
        public <T> T getDefault(Option<T> option) {
            // Makes sure the shared configuration settings are used kk default providers are called.
            if (sharedConfiguration.isSet(option)) {
                return sharedConfiguration.get(option);
            }
            return null;
        }
    }

    /**
     * Falls back on the shared configuration to provide a default value if an option is not set
     * anywhere and the local default providers don't know about the requested option.
     * This provider is meant to be the last default config provider.
     */
    static class SharedConfigurationDefaultProvider implements DefaultConfigProvider {

        private Configuration sharedConfiguration;

        public SharedConfigurationDefaultProvider(Configuration sharedConfiguration) {
            this.sharedConfiguration = sharedConfiguration;
        }

        @Override
        public <T> T getDefault(Option<T> option) {
            // The shared configuration is responsible for calling the global default providers and
            // returning the built-in default if all else fails.
            return sharedConfiguration.get(option);
        }
    }
}
