package net.sourceforge.vrapper.eclipse.mode;

import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;

/**
 * Base implementation of {@link PlatformSpecificModeProvider} to provide new command line
 * modes.
 * 
 * @author Bert Jacobs
 */
public abstract class AbstractEclipseSpecificModeProvider
        implements PlatformSpecificModeProvider {
    
    protected String name;
    
    public AbstractEclipseSpecificModeProvider(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
}
