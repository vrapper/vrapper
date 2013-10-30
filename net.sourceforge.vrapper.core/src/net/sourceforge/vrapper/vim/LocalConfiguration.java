package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.platform.Configuration;

public interface LocalConfiguration extends Configuration {

    public void removeListener(LocalConfigurationListener listener);

    public <T> void setLocal(Option<T> key, T value);

    public void addListener(LocalConfigurationListener listener);

}
