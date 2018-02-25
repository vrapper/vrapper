package net.sourceforge.vrapper.plugin.bindingkeeper;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.BundleContext;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.ViewPartListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.listener.VrapperListener;
import net.sourceforge.vrapper.plugin.bindingkeeper.preferences.PluginPreferenceStore;
import net.sourceforge.vrapper.plugin.bindingkeeper.preferences.PreferenceConstants;

/**
 * @author Pedro Santos
 * 
 */
public class BindingKeeper extends AbstractUIPlugin implements Runnable {
	private static final Map<CacheKey, Binding[]> CACHE = new HashMap<CacheKey, Binding[]>();
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
		PlatformUI.getWorkbench().getDisplay().syncExec(this);
	}

	@Override
	public void run() {
		boolean vrapperEnabled = VrapperListener.isVrapperEnabled();
		boolean activeEditor = hasActiveEditorView();
		boolean bindingKeeperEnabled = getPreferenceStore()
				.getBoolean(PreferenceConstants.P_DISABLE_UNWANTED_CONFLICTS);
		boolean insideActiveShell = viewListener.isInsideActiveShell();
		boolean showingPreferences = viewListener.isShowingPreferencesOrQuickAccess();

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
		IWorkbenchPart activePart = unwrap(page.getActivePart());

		return page != null && activePart instanceof AbstractTextEditor;
	}

	private void removeConflictingBindings() {
		if (active)
			return;// conflicting keys already removed

		// safe keeping current bindings
		preferenceStore
				.saveUserBindings(workbenchPreferenceStore.getString(IWorkbenchRegistryConstants.EXTENSION_COMMANDS));

		try {
			Binding[] currentBindings = bindingService.getBindings();
			List<String> blacklist = preferenceStore.getUnwantedConflicts();

			Binding[] nonConflictingBindings = disjointUnion(currentBindings, blacklist);
			bindingService.savePreferences(bindingService.getActiveScheme(), nonConflictingBindings);

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

	private IWorkbenchPart unwrap(IWorkbenchPart part) {
		if (part == null)
			return null;
		if (part instanceof MultiPageEditorPart) {
			Object selectedPage = ((MultiPageEditorPart) part).getSelectedPage();
			if (selectedPage instanceof IWorkbenchPart)
				return (IWorkbenchPart) selectedPage;
			else
				return null;
		} else {
			return part;
		}
	}

	private Binding[] disjointUnion(Binding[] currentBindings, List<String> blacklist) throws IOException {
		CacheKey key = new CacheKey(currentBindings, blacklist);

		if (CACHE.containsKey(key))
			return CACHE.get(key);

		ArrayList<Binding> bindings = new ArrayList<Binding>(asList(currentBindings));

		for (Iterator<Binding> i = bindings.iterator(); i.hasNext();)
			if (blacklist.contains(i.next().getTriggerSequence().toString()))
				i.remove();

		return CACHE.put(key, bindings.toArray(new Binding[0]));
	}

	static private class CacheKey {
		private Binding[] current;
		private List<String> blacklist;

		CacheKey(Binding[] current, List<String> blacklist) {
			this.current = current;
			this.blacklist = blacklist;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((blacklist == null) ? 0 : blacklist.hashCode());
			result = prime * result + Arrays.hashCode(current);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (blacklist == null) {
				if (other.blacklist != null)
					return false;
			} else if (!blacklist.equals(other.blacklist))
				return false;
			if (!Arrays.equals(current, other.current))
				return false;
			return true;
		}

	}
}
