package net.sourceforge.vrapper.eclipse.activator;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.log.Log;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;

/**
 * The activator class controls the plug-in life cycle
 */
public class VrapperPlugin extends AbstractUIPlugin implements /*IStartup,*/ Log {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.sourceforge.vrapper.eclipse";

    // The shared instance
    static VrapperPlugin plugin;

    private static boolean vrapperEnabled;

    private static final String KEY_VRAPPER_ENABLED = "vrapperEnabled";

    private static final String COMMAND_TOGGLE_VRAPPER = "net.sourceforge.vrapper.eclipse.commands.toggle";
    
    private static final IPreferencesService PREFERENCES_SERVICE = Platform.getPreferencesService();
    // private static final IEclipsePreferences PLUGIN_PREFERENCES = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
    /* XXX: The "new way" of creating InstanceScope was introduced in Eclipse Juno (4.2) so moving to the new way 
     * 		would actually break all backwards compatibility with the 3.x series of Eclipse. I'd rather not do 
     * 		that just yet. There are other solutions based on Eclipse (e.g., Aptana and Flash Builder) which are 
     *	    still on a 3.x version of Eclipse and I don't want to break Vrapper for all of those users until I 
     *      have to. I want to give everyone as long as possible to move to the 4.x series of Eclipse. 
     *      -- Github exchange with keforbes on why we're leaving this alone for now.
     */
    @SuppressWarnings("deprecation") // See note above
    private static final IEclipsePreferences PLUGIN_PREFERENCES = new InstanceScope().getNode(PLUGIN_ID);

	private static MouseButtonListener mouseButton = new MouseButtonListener();

    private boolean debugLogEnabled = Boolean.parseBoolean(System.getProperty(DEBUGLOG_PROPERTY))
            || Boolean.parseBoolean(Platform.getDebugOption("net.sourceforge.vrapper.eclipse/debug"));

    /**
     * The constructor
     */
    public VrapperPlugin() {
    }

    public InputInterceptor findActiveInterceptor()
            throws VrapperPlatformException, UnknownEditorException {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IEditorPart activeEditor = workbenchWindow.getActivePage().getActiveEditor();
        return InputInterceptorManager.INSTANCE.findActiveInterceptor(activeEditor);
    }

    /**
     * Returns the currently active Vrapper instance (InputInterceptor) for the given editor.
     * @param toplevelEditor IEditorPart to look for.
     * @return an InputInterceptor instance.
     * @throws VrapperPlatformException if Vrapper triggered errors while querying toplevelEditor.
     * @throws UnknownEditorException when this part did not have a corresponding Vrapper instance.
     */
    public InputInterceptor findActiveInterceptor(IEditorPart toplevelEditor)
            throws VrapperPlatformException, UnknownEditorException {
        return InputInterceptorManager.INSTANCE.findActiveInterceptor(toplevelEditor);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        VrapperLog.setImplementation(this);
    }

//    public void earlyStartup() {
//    }

    @Override
    public void stop(BundleContext context) throws Exception {
        preShutdown();
        plugin = null;
        VrapperLog.setImplementation(null);
        super.stop(context);
    }

    private void preShutdown() throws BackingStoreException {
    	storeVimEmulationOfActiveEditors();
    }

    void restoreVimEmulationInActiveEditors() {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow window: windows) {
            for (IWorkbenchPage page: window.getPages()) {
                for (IEditorReference ref: page.getEditorReferences()) {
                    InputInterceptorManager.INSTANCE.registerEditorRef(ref);
                    // Let the manager deal with any uninitialized editors.
                    InputInterceptorManager.INSTANCE.partOpened(ref);
                }
                if (page.getActiveEditor() != null) {
                    IWorkbenchPartReference partReference = page.getReference(page.getActiveEditor());
                    InputInterceptorManager.INSTANCE.partActivated(partReference);
                }
            }
        }
            
        addEditorListeners();
    }

    void addEditorListeners() {
        for (IWorkbenchWindow window: PlatformUI.getWorkbench().getWorkbenchWindows()) {
            addInterceptingListener(window);
        }
        PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
            public void windowOpened(IWorkbenchWindow window) {
                addInterceptingListener(window);
            }
            public void windowClosed(IWorkbenchWindow window) { }
            public void windowActivated(IWorkbenchWindow window) { }
            public void windowDeactivated(IWorkbenchWindow window) { }
        });
    }

    private static void addInterceptingListener(IWorkbenchWindow window) {
        window.getPartService().addPartListener(InputInterceptorManager.INSTANCE);
        window.getWorkbench().getDisplay().addFilter(SWT.MouseDown, mouseButton);
        window.getWorkbench().getDisplay().addFilter(SWT.MouseUp, mouseButton);
    }
    
    void addShutdownListener() {
        PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
            public void postShutdown(IWorkbench arg0) { }

            public boolean preShutdown(IWorkbench arg0, boolean arg1) {
                try {
                	storeVimEmulationOfActiveEditors();
                } catch (BackingStoreException e) {
                	VrapperLog.error("Error storing vrapper toggle state", e);
                }
                return true;
            }
        });
    }

    void activateVrapperShortcutContexts() {
        final IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);

        contextService.activateContext("net.sourceforge.vrapper.eclipse.enabledOnView", vrapperEnabledOnViewExpression(), true);

        contextService.activateContext("net.sourceforge.vrapper.eclipse.active", vrapperEnabledInAnyModeExpression(), true);
        contextService.activateContext("net.sourceforge.vrapper.eclipse.active.normal", vrapperEnabledInModeExpression(NormalMode.NAME), true);
        contextService.activateContext("net.sourceforge.vrapper.eclipse.active.command", vrapperEnabledInModeExpression(CommandLineMode.NAME), true);
        contextService.activateContext("net.sourceforge.vrapper.eclipse.active.visual", vrapperEnabledInModeExpression(VisualMode.NAME), true);
        contextService.activateContext("net.sourceforge.vrapper.eclipse.active.insert", vrapperEnabledInModeExpression(InsertMode.NAME), true);
    }

    private static Expression vrapperEnabledOnViewExpression() {
        return new Expression() {
            @Override
            public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
                Object currentlyActivePart = context.getVariable(ISources.ACTIVE_PART_NAME);
                boolean onAView = currentlyActivePart instanceof IViewPart;
                boolean vrapperEnabled = (Boolean) context.getVariable(VrapperStatusSourceProvider.SOURCE_ENABLED);
                return EvaluationResult.valueOf(vrapperEnabled && onAView);
            }

            @Override
            public void collectExpressionInfo(ExpressionInfo info) {
                info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
                info.addVariableNameAccess(VrapperStatusSourceProvider.SOURCE_ENABLED);
            }
        };
    }

    private static Expression vrapperEnabledInAnyModeExpression() {
        return new Expression() {
            @Override
            public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
                Object currentlyActivePart = context.getVariable(ISources.ACTIVE_PART_NAME);
                if ( ! (currentlyActivePart instanceof IEditorPart)) {
                    return EvaluationResult.FALSE;
                }
                Boolean vrapperEnabled = (Boolean) context.getVariable(VrapperStatusSourceProvider.SOURCE_ENABLED);
                String vrapperMode = (String) context.getVariable(VrapperStatusSourceProvider.SOURCE_CURRENTMODE);
                return EvaluationResult.valueOf(vrapperEnabled && ! VrapperStatusSourceProvider.MODE_UNKNOWN.equals(vrapperMode));
            }

            @Override
            public void collectExpressionInfo(ExpressionInfo info) {
                info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
                info.addVariableNameAccess(VrapperStatusSourceProvider.SOURCE_CURRENTMODE);
                info.addVariableNameAccess(VrapperStatusSourceProvider.SOURCE_ENABLED);
            }
        };
    }

    private static Expression vrapperEnabledInModeExpression(final String expectedModeName) {
        return new Expression() {
            @Override
            public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
                Object currentlyActivePart = context.getVariable(ISources.ACTIVE_PART_NAME);
                if ( ! (currentlyActivePart instanceof IEditorPart)) {
                    return EvaluationResult.FALSE;
                }
                Boolean vrapperEnabled = (Boolean) context.getVariable(VrapperStatusSourceProvider.SOURCE_ENABLED);
                String vrapperMode = (String) context.getVariable(VrapperStatusSourceProvider.SOURCE_CURRENTMODE);
                return EvaluationResult.valueOf(vrapperEnabled && expectedModeName.equals(vrapperMode));
            }

            @Override
            public void collectExpressionInfo(ExpressionInfo info) {
                info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
                info.addVariableNameAccess(VrapperStatusSourceProvider.SOURCE_CURRENTMODE);
                info.addVariableNameAccess(VrapperStatusSourceProvider.SOURCE_ENABLED);
            }
        };
    }

    private static void storeVimEmulationOfActiveEditors() throws BackingStoreException {
    	PLUGIN_PREFERENCES.clear();
    	PLUGIN_PREFERENCES.putBoolean(KEY_VRAPPER_ENABLED, vrapperEnabled);
    }

    void toggleVrapper() {
        boolean enable = PREFERENCES_SERVICE.getBoolean(PLUGIN_ID, KEY_VRAPPER_ENABLED, true, null);
        if (enable) {
            IHandlerService s = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
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

    @Override
    public void debug(String msg) {
        if (debugLogEnabled) {
            log(IStatus.INFO, msg, null);
        }
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        debugLogEnabled = enabled;
    }

    @Override
    public boolean isDebugEnabled() {
        return debugLogEnabled;
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
        for (InputInterceptor interceptor: InputInterceptorManager.INSTANCE.getInterceptors().values()) {
            EditorAdaptor adaptor = interceptor.getEditorAdaptor();
            if (adaptor != null) {
                adaptor.onChangeEnabled(enabled);
            }
        }
    }
    
    public static boolean isVrapperEnabled() {
        return vrapperEnabled;
    }
    
    public static boolean isMouseDown() {
    	return mouseButton.down;
    }
    
    private static final class MouseButtonListener implements Listener {
    	
    	private boolean down;

		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDown) {
				down = true;
			} else if (event.type == SWT.MouseUp) {
				down = false;
			}
		}
    }

}
