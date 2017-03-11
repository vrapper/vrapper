package net.sourceforge.vrapper.plugin.bindingkeeper.provider;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;

import net.sourceforge.vrapper.platform.PlatformVrapperLifecycleAdapter;
import net.sourceforge.vrapper.plugin.bindingkeeper.commands.BindingsKeeperListener;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * @author Pedro Santos
 *
 */
public class BindingKeeperLifecycleListener extends PlatformVrapperLifecycleAdapter {

	@Override
	public void editorConfigured(EditorAdaptor editorAdaptor, boolean enabled) {

		IBindingService bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();

		editorAdaptor.addVrapperEventListener(new BindingsKeeperListener(bindingService, commandService, preferenceStore));

	}
}
