package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.platform.Configuration.Option;

/** Listener which gets called when configuration changes in currently active editor. */
public interface LocalConfigurationListener {
    public <T> void optionChanged(Option<T> option, T oldValue, T newValue); 
}
