package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;

/**
 * Listener interface for classes which want to be notified if a dynamic delimiter was changed.
 */
public interface DelimiterChangedListener {
    public void delimiterChanged(DelimiterHolder from, DelimiterHolder to);
}
