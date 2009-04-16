package de.jroene.vrapper.eclipse;

import java.util.HashSet;

import kg.totality.core.Options;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
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

import de.jroene.vrapper.eclipse.interceptor.InputInterceptorFactory;
import de.jroene.vrapper.eclipse.interceptor.InputInterceptorManager;
import de.jroene.vrapper.eclipse.interceptor.VimInputInterceptorFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class VrapperPlugin extends AbstractUIPlugin implements IStartup {

    private static final String KEY_VIM_EMULATION_ACTIVE = "vimEmulationActive.";
    private static final String KEY_VRAPPER_ENABLED = "vrapperEnabled";

    // The plug-in ID
    public static final String PLUGIN_ID = "vrapper";

    // The shared instance
    private static VrapperPlugin plugin;
    private static boolean vrapperEnabled;

    private final HashSet<IEditorPart> editors;
    /**
     * The constructor
     */
    public VrapperPlugin() {
        editors = new HashSet<IEditorPart>();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
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

    // TODO:unify Vrapper and Totality preferences
    // TODO: this one isn't ever called
	private void initDefaults() {
		getPreferenceStore().setDefault(Options.STUPID_Y, true);
		getPreferenceStore().setDefault(Options.STUPID_CW, true);
	}

	@Override
    public void earlyStartup() {
        getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                toggleVrapper();
            }
        });
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static VrapperPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public void registerEditor(IEditorPart part) {
        editors.add(part);
    }

    public void unregisterEditor(IEditorPart part) {
        editors.remove(part);
    }

    public static boolean isVrapperEnabled() {
        return vrapperEnabled;
    }

    public static void setVrapperEnabled(boolean vrapperEnabled) {
        VrapperPlugin.vrapperEnabled = vrapperEnabled;
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

    private void addShutdownListener() {
        getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
            public void postShutdown(IWorkbench arg0) { }

            public boolean preShutdown(IWorkbench arg0, boolean arg1) {
                storeVimEmulationOfActiveEditors();
                return true;
            }
        });
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

	private static void log(int status, String msg, Throwable exception) {
		VrapperPlugin instance = getDefault();
		if (instance != null)
			instance.getLog().log(new Status(status, PLUGIN_ID, msg, exception));
		else if (status == IStatus.ERROR)
			System.err.println(msg);
		else
			System.out.println(msg);

	}

	public static void info(String msg) {
		log(IStatus.INFO, msg, null);
	}

	public static void error(String msg, Throwable exception) {
		log(IStatus.ERROR, msg, exception);
	}

	public static void error(String msg) {
		log(IStatus.ERROR, msg, null);
	}

	public static Exception coreException(String msg, Throwable exception) {
		return new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, msg, exception));
	}

}
