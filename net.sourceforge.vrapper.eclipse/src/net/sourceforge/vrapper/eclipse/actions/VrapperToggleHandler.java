package net.sourceforge.vrapper.eclipse.actions;

import java.util.Map;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * The action which is available in the menu and toolbar to activate the
 * vim-like behaviour on newly created editors.
 */
public class VrapperToggleHandler extends AbstractHandler implements
        IElementUpdater {
    /**
     * adds / removes the listener and sets the actions "checked" status
     * accordingly.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!VrapperPlugin.isVrapperEnabled())
            VrapperPlugin.setVrapperEnabled(true);
        else
            VrapperPlugin.setVrapperEnabled(false);

        ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        service.refreshElements(event.getCommand().getId(), null);
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void updateElement(UIElement element, Map map) {
        element.setChecked(VrapperPlugin.isVrapperEnabled());
    }

}
