package net.sourceforge.vrapper.plugin.bindingkeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.ViewPartListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.VrapperListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.preferences.PreferenceConstants;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author Pedro Santos
 * 
 */
public class BindingKeeper extends AbstractUIPlugin implements IStartup {

	private static BindingKeeper instance;
	private IBindingService bindingService;
	private ICommandService commandService;
	private IPreferenceStore workbenchPreferenceStore;

	public BindingKeeper() {
		instance = this;
		bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		workbenchPreferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	public static BindingKeeper getDefault() {
		return instance;
	}

	@Override
	public void earlyStartup() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				Workbench.getInstance().getActiveWorkbenchWindow().getPartService().addPartListener(new ViewPartListener());
			}
		});
	}

	public void setupBindings() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		// TODO test for AbstractTextEditor inside multi part editors
		boolean activeEditor = page.getActivePart() instanceof AbstractTextEditor;
		boolean vrapperEnabled = VrapperListener.vrapperEnabled;
		boolean bindingKeeperEnabled = getPreferenceStore().getBoolean(PreferenceConstants.P_DISABLE_CONFLICTING_SHORTCUTS);

		if (!bindingKeeperEnabled || !activeEditor) {
			restoreUserBindings();
			return;
		}

		if (vrapperEnabled)
			removeConflictingBindings();
		else
			restoreUserBindings();

	}

	private void removeConflictingBindings() {

		storeUserBindings();

		ArrayList<Binding> bindings = new ArrayList<Binding>(Arrays.asList(bindingService.getBindings()));

		String configuredBlacklist = getPreferenceStore().getString(PreferenceConstants.P_CONFLICTING_SHORTCUTS);
		Collection<String> blacklist = Arrays.asList(configuredBlacklist.split(":"));

		for (Iterator<Binding> i = bindings.iterator(); i.hasNext();)
			if (blacklist.contains(i.next().getTriggerSequence().toString()))
				i.remove();

		try {
			bindingService.savePreferences(bindingService.getActiveScheme(), bindings.toArray(new Binding[0]));
		} catch (IOException e) {
			VrapperLog.error("Binding keeper plugin were unable to clean conflicting key bindings", e);
		}
		VrapperLog.debug("Conflicting key bindings removed");
	}

	private void restoreUserBindings() {

		loadStoredUserBindings();

		bindingService.readRegistryAndPreferences(commandService);

		VrapperLog.debug("User's keybinding restored");
	}

	private void loadStoredUserBindings() {
		String keptBindings = getPreferenceStore().getString(PreferenceConstants.P_USER_SHORTCUTS);

		if (keptBindings != null && !keptBindings.isEmpty()) {
			workbenchPreferenceStore.setValue(IWorkbenchRegistryConstants.EXTENSION_COMMANDS, keptBindings);
			getPreferenceStore().setValue(PreferenceConstants.P_USER_SHORTCUTS, "");
		}
	}

	private void storeUserBindings() {
		String keptBindings = getPreferenceStore().getString(PreferenceConstants.P_USER_SHORTCUTS);

		if (keptBindings == null || keptBindings.isEmpty())
			getPreferenceStore().setValue(PreferenceConstants.P_USER_SHORTCUTS,
					workbenchPreferenceStore.getString(IWorkbenchRegistryConstants.EXTENSION_COMMANDS));

		// TODO: restore any new user binding add while Vrapper was enabled

	}

}
