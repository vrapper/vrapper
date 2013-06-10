package net.sourceforge.vrapper.plugin.window.commands;

import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractWindowCommand extends CountIgnoringNonRepeatableCommand {

    protected IWorkbenchPartSite getEditorSite() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActivePart().getSite();
    }

}
