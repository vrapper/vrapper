package de.jroene.vrapper.eclipse.actions;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.jroene.vrapper.eclipse.Activator;
import de.jroene.vrapper.eclipse.interceptor.InputInterceptorManager;
import de.jroene.vrapper.eclipse.interceptor.VimInputInterceptorFactory;

/**
 * The action which is available in the menu and toolbar to activate the
 * vim-like behaviour on newly created editors.
 */
public class VrapperToggleHandler extends AbstractHandler implements IElementUpdater {
    private IWorkbenchWindow window;
    private InputInterceptorManager manager;
    /**
     * The constructor.
     */
    public VrapperToggleHandler() {
        manager = null;
    }

    /**
     * adds / removes the listener and sets the actions "checked" status
     * accordingly.
     */
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if(manager != null) {
            disable();
        } else {
            enable();
        }
        ICommandService service =
            (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        service.refreshElements(arg0.getCommand().getId(), null);

        return null;
    }

    private void disable() {
        manager.deactivate();
        manager = null;
        Activator.setVrapperEnabled(false);
    }

    private void enable() {
        IPartService service = window.getPartService();
        manager = new InputInterceptorManager(new VimInputInterceptorFactory(), window);
        service.addPartListener(manager);
        Activator.setVrapperEnabled(true);
    }

    @SuppressWarnings("unchecked")
    public void updateElement(UIElement arg0, Map arg1) {
        arg0.setChecked(manager != null);
    }

}
