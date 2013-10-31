package net.sourceforge.vrapper.vim;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SimpleConfiguration;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;

/** Wraps a {@link Configuration}, allowing to notify {@link LocalConfigurationListener}. */
public class SimpleLocalConfiguration implements LocalConfiguration {
    
    protected Configuration sharedConfiguration;
    protected SimpleConfiguration localConfiguration = new SimpleConfiguration();
    
    protected String newLine;
    
    protected List<LocalConfigurationListener> listeners =
            new CopyOnWriteArrayList<LocalConfigurationListener>();

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
        sharedConfiguration.set(key, value);
            for (LocalConfigurationListener listener : listeners) {
                listener.optionChanged(key, oldValue, value);
            }
    }

    @Override
    public <T> void setLocal(Option<T> key, T value) {
        localConfiguration.set(key, value);
    }

    public <T> T get(Option<T> key) {
        if (localConfiguration.isSet(key)) {
            return localConfiguration.get(key);
        } else {
            return sharedConfiguration.get(key);
        }
    }
    
    public void addListener(LocalConfigurationListener listener) {
        listeners.add(listener);
    }
 
    public void removeListener(LocalConfigurationListener listener) {
        listeners.remove(listener);
    }

}
