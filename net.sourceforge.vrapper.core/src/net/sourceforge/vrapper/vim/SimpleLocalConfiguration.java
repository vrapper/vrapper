package net.sourceforge.vrapper.vim;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SimpleConfiguration;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;

/** Wraps a {@link Configuration}, allowing to notify {@link ConfigurationListener}. */
public class SimpleLocalConfiguration implements LocalConfiguration {
    
    protected Configuration sharedConfiguration;
    protected SimpleConfiguration localConfiguration = new SimpleConfiguration();
    
    protected String newLine;
    
    protected List<ConfigurationListener> listeners =
            new CopyOnWriteArrayList<ConfigurationListener>();
    private boolean listenersEnabled;

    public SimpleLocalConfiguration(Configuration configuration) {
        sharedConfiguration = configuration;
        //Don't share the newline.  Each file has its own newline.
        //(in case you have one windows file open
        //  and one unix file open at the same time)
        newLine = sharedConfiguration.getNewLine();
    }

    public String getNewLine() {
        return newLine;
    }

    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }

    public void setNewLine(NewLine newLine) {
        this.newLine = newLine.nl;
    }

    public <T> void set(Option<T> key, T value) {
        T oldValue = sharedConfiguration.get(key);
        if ( ! key.getScope().equals(OptionScope.LOCAL)) {
            sharedConfiguration.set(key, value);
        }
        if (localConfiguration.isSet(key) || key.getScope().equals(OptionScope.LOCAL)) {
            oldValue = localConfiguration.get(key);
            localConfiguration.set(key, value);
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
            T oldValue = localConfiguration.get(key);
            if (key.getScope().equals(OptionScope.DEFAULT) && oldValue == null) {
                oldValue = sharedConfiguration.get(key);
            }
            localConfiguration.set(key, value);
            if (listenersEnabled) {
                for (ConfigurationListener listener : listeners) {
                    listener.optionChanged(key, oldValue, value);
                }
            }
        }
    }

    public <T> T get(Option<T> key) {
        if (localConfiguration.isSet(key)) {
            return localConfiguration.get(key);
        } else {
            return sharedConfiguration.get(key);
        }
    }
    
    @Override
    public <T> boolean isSet(Option<T> key) {
        return localConfiguration.isSet(key);
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

}
