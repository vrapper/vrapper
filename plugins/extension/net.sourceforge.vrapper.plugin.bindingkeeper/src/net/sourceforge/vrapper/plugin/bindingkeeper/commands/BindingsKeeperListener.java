package net.sourceforge.vrapper.plugin.bindingkeeper.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;

/**
 * @author Pedro Santos
 * 
 */
public class BindingsKeeperListener extends VrapperEventAdapter {
	private static final String USER_BINDINGS = "bindingskeeper.usersettings";

	private static List<String> blacklist = Arrays.asList(//
			"CTRL+A", "CTRL+X", // increment/decrement numbers
			"CTRL+E", "CTRL+Y", "CTRL+D", "CTRL+U", "CTRL+F", "CTRL+B", // navigation
			"CTRL+P", "CTRL+N", // auto complete
			"CTRL+V", // selection
			"CTRL+R", // paste in insert mode
			"CTRL+H", "CTRL+W", "CTRL-U", // remove text in insert mode
			"CTRL+J", "CTRL+M"// new line in insert mode
	);

	private IBindingService bindingService;
	private ICommandService commandService;
	private IPreferenceStore preferenceStore;

	public BindingsKeeperListener(IBindingService bindingService, ICommandService commandService, IPreferenceStore preferenceStore) {
		this.bindingService = bindingService;
		this.commandService = commandService;
		this.preferenceStore = preferenceStore;
	}

	@Override
	public void vrapperToggled(boolean enabled) {
		if (enabled)
			removeConflictingKeyBindings();
		else
			restoreUserBindings();

	}

	private void removeConflictingKeyBindings() {

		storeUserBindings();

		ArrayList<Binding> bindings = new ArrayList<Binding>(Arrays.asList(bindingService.getBindings()));

		for (Iterator<Binding> i = bindings.iterator(); i.hasNext();)
			if (blacklist.contains(i.next().getTriggerSequence().toString()))
				i.remove();

		try {
			bindingService.savePreferences(bindingService.getActiveScheme(), bindings.toArray(new Binding[0]));
		} catch (IOException e) {
			VrapperLog.error("Binding keeper plugin were unable to clean conflicting key bindings", e);
		}

	}

	private void restoreUserBindings() {

		loadStoredUserBindings();

		bindingService.readRegistryAndPreferences(commandService);
	}

	private void loadStoredUserBindings() {
		String keptBindings = preferenceStore.getString(USER_BINDINGS);

		if (keptBindings != null && !keptBindings.isEmpty()) {
			preferenceStore.setValue(IWorkbenchRegistryConstants.EXTENSION_COMMANDS, keptBindings);
			preferenceStore.setValue(USER_BINDINGS, "");
		}
	}

	private void storeUserBindings() {
		String keptBindings = preferenceStore.getString(USER_BINDINGS);

		if (keptBindings == null || keptBindings.isEmpty())
			preferenceStore.setValue(USER_BINDINGS, preferenceStore.getString(IWorkbenchRegistryConstants.EXTENSION_COMMANDS));

		// TODO: restore any new user binding add while Vrapper was enabled

	}

}
