package net.sourceforge.vrapper.eclipse.activator;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

public class VrapperStartup implements IStartup {

    @Override
    public void earlyStartup() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                VrapperPlugin plugin = VrapperPlugin.plugin;
                plugin.restoreVimEmulationInActiveEditors();
                plugin.addEditorListeners();
                plugin.addShutdownListener();
                plugin.activateVrapperShortcutContexts();
                plugin.toggleVrapper();
            }
        });
    }

}
