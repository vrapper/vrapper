package net.sourceforge.vrapper.platform;

/**
 * Base implementation of {@link PlatformSpecificModeProvider} to provide new command line
 * modes.
 */
public abstract class AbstractPlatformSpecificModeProvider
        implements PlatformSpecificModeProvider {
    
    protected String name;
    
    public AbstractPlatformSpecificModeProvider(String name) {
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
