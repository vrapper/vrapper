package net.sourceforge.vrapper.eclipse.activator;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.log.Log;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class VrapperPlugin extends AbstractUIPlugin implements IStartup, Log {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.sourceforge.vrapper.eclipse";

    // The shared instance
    private static VrapperPlugin plugin;

    private static boolean vrapperEnabled;

    private static final String KEY_VRAPPER_ENABLED = "vrapperEnabled";

    private static final String COMMAND_TOGGLE_VRAPPER = "net.sourceforge.vrapper.eclipse.commands.toggle";

    /**
     * The constructor
     */
    public VrapperPlugin() {
    }

    public void registerEditor(IEditorPart part) { }

    public void unregisterEditor(IEditorPart part) { }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        VrapperLog.setImplementation(this);
        getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                restoreVimEmulationInActiveEditors();
                addEditorListeners();
                addShutdownListener();
            }
        });
    }

    public void earlyStartup() {
        getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                toggleVrapper();
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        VrapperLog.setImplementation(null);
        super.stop(context);
    }

    private void restoreVimEmulationInActiveEditors() {
        IWorkbenchWindow[] windows = plugin.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow window: windows) {
            for (IWorkbenchPage page: window.getPages()) {
                for (IEditorReference ref: page.getEditorReferences()) {
                    IEditorPart part = ref.getEditor(true);
                    if (part != null) {
                        InputInterceptorManager.INSTANCE.interceptWorkbenchPart(part);
                    }
                }
            }
        }
            
        addEditorListeners();
    }

    private void addEditorListeners() {
        for (IWorkbenchWindow window: plugin.getWorkbench().getWorkbenchWindows()) {
            addInterceptingListener(window);
        }
        plugin.getWorkbench().addWindowListener(new IWindowListener() {
            
            public void windowOpened(IWorkbenchWindow window) {
                addInterceptingListener(window);
            }
            
            public void windowDeactivated(IWorkbenchWindow window) { }
            public void windowClosed(IWorkbenchWindow window) { }
            public void windowActivated(IWorkbenchWindow window) { }
        });
    }

    private static void addInterceptingListener(IWorkbenchWindow window) {
        window.getPartService().addPartListener(InputInterceptorManager.INSTANCE);
    }

    private void addShutdownListener() {
        getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
            public void postShutdown(IWorkbench arg0) { }

            public boolean preShutdown(IWorkbench arg0, boolean arg1) {
                storeVimEmulationOfActiveEditors();
                return true;
            }
        });
    }

    private void storeVimEmulationOfActiveEditors() {
        Preferences p = plugin.getPluginPreferences();
        // clean preferences
        for (String key : p.propertyNames()) {
            p.setToDefault(key);
        }
        p.setValue(KEY_VRAPPER_ENABLED, vrapperEnabled);
        plugin.savePluginPreferences();
    }

    private void toggleVrapper() {
        boolean enable = getPluginPreferences().getBoolean(KEY_VRAPPER_ENABLED);
        if (enable) {
            IHandlerService s = (IHandlerService) getWorkbench().getService(IHandlerService.class);
            try {
                s.executeCommand(COMMAND_TOGGLE_VRAPPER, null);
            } catch (Exception e) {
                VrapperLog.error("Error when toggling Vrapper", e);
            }
        }
    }

    private static void log(int status, String msg, Throwable exception) {
        VrapperPlugin instance = getDefault();
        if (instance != null) {
            instance.getLog().log(new Status(status, PLUGIN_ID, msg, exception));
        } else if (status == IStatus.ERROR) {
            System.err.println(msg);
        } else {
            System.out.println(msg);
        }

    }

    public void info(String msg) {
        log(IStatus.INFO, msg, null);
    }

    public void error(String msg, Throwable exception) {
        log(IStatus.ERROR, msg, exception);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static VrapperPlugin getDefault() {
        return plugin;
    }

    public static void setVrapperEnabled(boolean enabled) {
        vrapperEnabled = enabled;
        for (InputInterceptor interceptor: InputInterceptorManager.INSTANCE.getInterceptors()) {
            EditorAdaptor adaptor = interceptor.getEditorAdaptor();
            if (adaptor != null)
                adaptor.onChangeEnabled(enabled);
        }
    }
    
    public static boolean isVrapperEnabled() {
        return vrapperEnabled;
    }

}
