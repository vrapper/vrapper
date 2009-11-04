package net.sourceforge.vrapper.eclipse.matcher;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class EclipseSpecificStateProviderFactory {

    public static AbstractEclipseSpecificStateProvider create(IConfigurationElement configurationElement) {
        IConfigurationElement[] children = configurationElement.getChildren("java-class");
        if (children.length != 1)
            throw new RuntimeException("schema WTF");
        try {
            return (AbstractEclipseSpecificStateProvider) children[0].createExecutableExtension("class");
        } catch (CoreException e) {
            VrapperLog.error("couldn't create PlatformSpecificStateProvider", e);
        }
        return null;
    }

}
