package net.sourceforge.vrapper.plugin.bindingkeeper;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.plugin.bindingkeeper.listener.VrapperListener.vrapperEnabled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.BundleContext;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.ViewPartListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.preferences.PluginPreferenceStore;
import net.sourceforge.vrapper.plugin.bindingkeeper.preferences.PreferenceConstants;

/**
 * @author Pedro Santos
 * 
 */
public class BindingKeeper extends AbstractUIPlugin {

	private static BindingKeeper instance;
	private IBindingService bindingService;
	private ICommandService commandService;
	private PluginPreferenceStore preferenceStore;
	private IPreferenceStore workbenchPreferenceStore;
	private ViewPartListener viewListener = new ViewPartListener();
	private boolean active;

	public BindingKeeper() {
		instance = this;
		bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		workbenchPreferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		preferenceStore = new PluginPreferenceStore(
				(ScopedPreferenceStore) BindingKeeper.getDefault().getPreferenceStore());
	}

	public static BindingKeeper getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		PlatformUI.getWorkbench().getDisplay().asyncExec(viewListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			restoreUserBindings();
		} finally {
			super.stop(context);
		}
	}

	public void setupBindings() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				setupBindingsOnUiThread();
			}
		});
	}

	public void setupBindingsOnUiThread() {
		boolean activeEditor = hasActiveEditorView();
		boolean bindingKeeperEnabled = getPreferenceStore()
				.getBoolean(PreferenceConstants.P_DISABLE_UNWANTED_CONFLICTS);
		boolean insideActiveShell = viewListener.isInsideActiveShell();
		boolean showingPreferences = viewListener.isShowingPreferences();

		if (vrapperEnabled && bindingKeeperEnabled && activeEditor && insideActiveShell && !showingPreferences)
			removeConflictingBindings();
		else
			restoreUserBindings();

	}

	private boolean hasActiveEditorView() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win == null)
			win = wb.getWorkbenchWindows()[0];
		IWorkbenchPage page = win.getActivePage();
		// TODO test for AbstractTextEditor inside multi part editors
		return page != null && page.getActivePart() instanceof AbstractTextEditor;
	}

	private void removeConflictingBindings() {
		if (active)
			return;// conflicting keys already removed

		// safe keeping current bindings
		preferenceStore
				.saveUserBindings(workbenchPreferenceStore.getString(IWorkbenchRegistryConstants.EXTENSION_COMMANDS));

		ArrayList<Binding> bindings = new ArrayList<Binding>(asList(bindingService.getBindings()));

		Collection<String> blacklist = preferenceStore.getUnwantedConflicts();

		for (Iterator<Binding> i = bindings.iterator(); i.hasNext();)
			if (blacklist.contains(i.next().getTriggerSequence().toString()))
				i.remove();

		try {
			bindingService.savePreferences(bindingService.getActiveScheme(), bindings.toArray(new Binding[0]));
			active = true;
			VrapperLog.debug("Conflicting key bindings removed");
		} catch (IOException e) {
			VrapperLog.error("Binding keeper plugin were unable to clean conflicting key bindings", e);
		}
	}

	private void restoreUserBindings() {

		if (!active)
			return;// plugin changes were already reverted

		workbenchPreferenceStore.setValue(IWorkbenchRegistryConstants.EXTENSION_COMMANDS,
				preferenceStore.getUserBindings());

		bindingService.readRegistryAndPreferences(commandService);

		active = false;
		VrapperLog.debug("User's keybinding restored");
	}

}
