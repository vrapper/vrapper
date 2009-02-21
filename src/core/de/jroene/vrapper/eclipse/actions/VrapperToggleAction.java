package de.jroene.vrapper.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import de.jroene.vrapper.eclipse.interceptor.InputInterceptorManager;
import de.jroene.vrapper.eclipse.interceptor.VimInputInterceptorFactory;

/**
 * The action which is available in the menu and toolbar to activate the
 * vim-like behaviour on newly created editors.
 */
public class VrapperToggleAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;
    private InputInterceptorManager manager;
    /**
     * The constructor.
     */
    public VrapperToggleAction() {
        manager = null;
    }

    /**
     * adds / removes the listener and sets the actions "checked" status
     * accordingly.
     */
    public void run(IAction action) {
        if(manager != null) {
            disable();
        } else {
            enable();
        }
        action.setChecked(manager != null);

    }

    private void disable() {
        IPartService service = window.getPartService();
        service.removePartListener(manager);
        manager = null;
    }

    private void enable() {
        IPartService service = window.getPartService();
        manager = new InputInterceptorManager(new VimInputInterceptorFactory(), window);
        service.addPartListener(manager);
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
        //        if (Activator.getDefault().getPluginPreferences().getBoolean("action.toggle.enabled")) {
        //        enable();
        //        }
    }
}