package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;

/**
 * @author Pedro Santos
 * 
 */
public class VrapperListener extends VrapperEventAdapter {
	public static boolean vrapperEnabled;

	@Override
	public void vrapperToggled(boolean enabled) {
		vrapperEnabled = enabled;
		BindingKeeper.getDefault().setupBindings();
	}

}
