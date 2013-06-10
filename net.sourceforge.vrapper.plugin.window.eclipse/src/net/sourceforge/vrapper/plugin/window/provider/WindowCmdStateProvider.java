package net.sourceforge.vrapper.plugin.window.provider;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;

import org.eclipse.ui.IWorkbenchPartSite;

public class WindowCmdStateProvider extends AbstractEclipseSpecificStateProvider {
    
    IWorkbenchPartSite editorSite;

    public WindowCmdStateProvider() {
    }

}
