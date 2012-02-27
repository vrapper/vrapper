package net.sourceforge.vrapper.vim;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;

/** Wraps a {@link Configuration}, allowing to notify {@link LocalConfigurationListener}. */
public class SimpleLocalConfiguration implements LocalConfiguration {
    protected Configuration sharedConfiguration;
    protected List<LocalConfigurationListener> listeners =
            new CopyOnWriteArrayList<LocalConfigurationListener>();

    public SimpleLocalConfiguration(Configuration configuration) {
        sharedConfiguration = configuration;
    }

    public String getNewLine() {
        return sharedConfiguration.getNewLine();
    }

    public void setNewLine(String newLine) {
        sharedConfiguration.setNewLine(newLine);
    }

    public void setNewLine(NewLine newLine) {
        sharedConfiguration.setNewLine(newLine);
    }

    public <T> void set(Option<T> key, T value) {
        T oldValue = sharedConfiguration.get(key);
        sharedConfiguration.set(key, value);
            for (LocalConfigurationListener listener : listeners) {
                listener.optionChanged(key, oldValue, value);
            }
    }

    public <T> T get(Option<T> key) {
        return sharedConfiguration.get(key);
    }
    
    public void addListener(LocalConfigurationListener listener) {
        listeners.add(listener);
    }
 
    public void removeListener(LocalConfigurationListener listener) {
        listeners.remove(listener);
    }
}
