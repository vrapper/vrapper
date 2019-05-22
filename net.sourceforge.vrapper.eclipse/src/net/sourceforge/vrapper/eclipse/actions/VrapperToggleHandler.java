package net.sourceforge.vrapper.eclipse.actions;

import java.util.Map;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.activator.VrapperStatusSourceProvider;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * The action which is available in the menu and toolbar to activate the
 * vim-like behaviour on newly created editors.
 */
public class VrapperToggleHandler extends AbstractHandler implements IElementUpdater {
    /**
     * adds / removes the listener and sets the actions "checked" status
     * accordingly.
     */
    public Object execute(ExecutionEvent event) {

        boolean targetEnabledFlag = !VrapperPlugin.isVrapperEnabled();
        VrapperPlugin.setVrapperEnabled(targetEnabledFlag);

        IWorkbench workbench = PlatformUI.getWorkbench();

        // Fire listeners on source provider so they know the enabled status was changed
        ISourceProviderService sourceProviderService =
                (ISourceProviderService) workbench.getService(ISourceProviderService.class);
        VrapperStatusSourceProvider sourceProvider = (VrapperStatusSourceProvider)
                sourceProviderService.getSourceProvider(VrapperStatusSourceProvider.SOURCE_ENABLED);
        sourceProvider.fireVrapperEnabledChange(targetEnabledFlag);

        ICommandService service = (ICommandService) workbench.getService(ICommandService.class);
        service.refreshElements(event.getCommand().getId(), null);
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void updateElement(UIElement element, Map map) {
        element.setChecked(VrapperPlugin.isVrapperEnabled());
    }

}
