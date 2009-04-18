package net.sourceforge.vrapper.eclipse.activator;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorFactory;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.eclipse.interceptor.VimInputInterceptorFactory;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStartup;
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
public class Activator extends AbstractUIPlugin implements IStartup {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.sourceforge.vrapper.eclipse";

    // The shared instance
    private static Activator plugin;

    private final Set<IEditorPart> editors = new HashSet<IEditorPart>();

    private static boolean vrapperEnabled;

    private static final String KEY_VIM_EMULATION_ACTIVE = "vimEmulationActive.";

    private static final String KEY_VRAPPER_ENABLED = "vrapperEnabled";

    /**
     * The constructor
     */
    public Activator() {
    }

    public void registerEditor(IEditorPart part) {
        editors.add(part);
    }

    public void unregisterEditor(IEditorPart part) {
        editors.remove(part);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                restoreVimEmulationInActiveEditors();
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
        super.stop(context);
    }

    private void restoreVimEmulationInActiveEditors() {
        // FIXME: iterate over all the windows
        IWorkbenchWindow w = plugin.getWorkbench().getWorkbenchWindows()[0];
        Preferences preferences = plugin.getPluginPreferences();
        InputInterceptorFactory f = new VimInputInterceptorFactory();
        InputInterceptorManager manager = new InputInterceptorManager(f, w);
        boolean addListener = false;
        for (IWorkbenchPage page : w.getPages()) {
            for (IEditorReference ref : page.getEditorReferences()) {
                String key = createKey(ref.getName());
                boolean activate = preferences.getBoolean(key);
                IEditorPart part = ref.getEditor(true);
                if (activate) {
                    if (part != null) {
                        manager.partOpened(part);
                        addListener = true;
                    } else {
                        preferences.setToDefault(key);
                    }
                }
            }
        }
        if (addListener) {
            manager.deactivate();
            w.getPartService().addPartListener(manager);
        }
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

    private static String createKey(String name) {
        return KEY_VIM_EMULATION_ACTIVE + name;
    }

    private void storeVimEmulationOfActiveEditors() {
        Preferences p = plugin.getPluginPreferences();
        // clean preferences
        for (String key : p.propertyNames()) {
            p.setToDefault(key);
        }
        // store which opened editors have vim emulation active
        for (IEditorPart part : editors) {
            String key = createKey(part.getEditorInput().getName());
            p.setValue(key, true);
        }
        p.setValue(KEY_VRAPPER_ENABLED, vrapperEnabled);
        plugin.savePluginPreferences();
    }

    private void toggleVrapper() {
        // FIXME: this causes NullPointerException to be thrown
        boolean enable = getPluginPreferences().getBoolean(KEY_VRAPPER_ENABLED);
        if (enable) {
            IHandlerService s = (IHandlerService) getWorkbench().getService(IHandlerService.class);
            try {
                s.executeCommand("de.jroene.vrapper.commands.toggle", null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public static void setVrapperEnabled(boolean enabled) {
        vrapperEnabled = enabled;
    }

}
