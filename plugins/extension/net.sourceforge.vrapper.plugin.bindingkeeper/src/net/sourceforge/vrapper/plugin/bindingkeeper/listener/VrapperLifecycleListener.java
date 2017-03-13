package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import net.sourceforge.vrapper.platform.PlatformVrapperLifecycleAdapter;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * @author Pedro Santos
 * 
 */
public class VrapperLifecycleListener extends PlatformVrapperLifecycleAdapter {

	@Override
	public void editorConfigured(EditorAdaptor editorAdaptor, boolean enabled) {
		editorAdaptor.addVrapperEventListener(new VrapperListener());

	}
}
