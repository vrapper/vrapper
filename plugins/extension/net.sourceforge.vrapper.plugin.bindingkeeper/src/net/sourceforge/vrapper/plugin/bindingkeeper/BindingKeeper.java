package net.sourceforge.vrapper.plugin.bindingkeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.ViewPartListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.VrapperListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.preferences.PreferenceConstants;

/**
 * @author Pedro Santos
 * 
 */
public class BindingKeeper extends AbstractUIPlugin implements IStartup {

	private static BindingKeeper instance;
	private IBindingService bindingService;
	private ICommandService commandService;
	private IPreferenceStore workbenchPreferenceStore;
	private IPreferenceStore bindingkeeperPreferenceStore;
	private ViewPartListener viewListener = new ViewPartListener();

	public BindingKeeper() {
		instance = this;
		bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		workbenchPreferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		bindingkeeperPreferenceStore = getPreferenceStore();
	}

	public static BindingKeeper getDefault() {
		return instance;
	}

	@Override
	public void earlyStartup() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(viewListener);
	}

	public void setupBindings() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		// TODO test for AbstractTextEditor inside multi part editors
		boolean activeEditor = page.getActivePart() instanceof AbstractTextEditor;
		boolean bindingKeeperEnabled = getPreferenceStore().getBoolean(PreferenceConstants.P_DISABLE_UNWANTED_CONFLICTS);
		boolean insideActiveShell = viewListener.isInsideActiveShell();

		if (!VrapperListener.vrapperEnabled || !bindingKeeperEnabled || !activeEditor || !insideActiveShell)
			restoreUserBindings();
		else
			removeConflictingBindings();

	}

	private void removeConflictingBindings() {

		String storedUserBindings = getPreferenceStore().getString(PreferenceConstants.P_USER_BINDINGS);

		if (storedUserBindings != null && !storedUserBindings.isEmpty())
			return;// conflicting keys already removed

		storedUserBindings = workbenchPreferenceStore.getString(IWorkbenchRegistryConstants.EXTENSION_COMMANDS);
		bindingkeeperPreferenceStore.setValue(PreferenceConstants.P_USER_BINDINGS, storedUserBindings);

		ArrayList<Binding> bindings = new ArrayList<Binding>(Arrays.asList(bindingService.getBindings()));

		String configuredBlacklist = bindingkeeperPreferenceStore.getString(PreferenceConstants.P_UNWANTED_CONFLICTS);
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

		String storedUserBindings = getPreferenceStore().getString(PreferenceConstants.P_USER_BINDINGS);

		if (storedUserBindings == null || storedUserBindings.trim().isEmpty())
			return;// current bindings were already set by user

		Set<Binding> newUserBindings = new HashSet<Binding>();
		for (Binding b : bindingService.getBindings())
			if (b.getType() == Binding.USER)
				newUserBindings.add(b);

		workbenchPreferenceStore.setValue(IWorkbenchRegistryConstants.EXTENSION_COMMANDS, storedUserBindings);
		bindingkeeperPreferenceStore.setValue(PreferenceConstants.P_USER_BINDINGS, "");

		bindingService.readRegistryAndPreferences(commandService);

		List<Binding> restoredBindinds = Arrays.asList(bindingService.getBindings());
		if (!restoredBindinds.containsAll(newUserBindings)) {
			Set<Binding> union = new HashSet<Binding>();
			union.addAll(newUserBindings);
			union.addAll(restoredBindinds);
			try {
				bindingService.savePreferences(bindingService.getActiveScheme(), union.toArray(new Binding[0]));
			} catch (IOException e) {
				VrapperLog.error("Unable to restore Users key bindings", e);
			}
		}

		VrapperLog.debug("User's keybinding restored");
	}

}
