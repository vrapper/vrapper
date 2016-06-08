package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

public interface PlatformSpecificVolatileStateProvider {
    
    /**
     * Priority of volatile stuff is... volatile. It can change suddenly, so no hardcoding in
     * plgin.xml. Anyway, declaring things in plugin manifest file seems to make sense only for
     * scenarios like displaying something in GUI but not loading corresponding code until user
     * actually clicks the thing.
     */
    int getVolatilePriority();
    
    EvaluatorMapping getVolatileCommands();
}
