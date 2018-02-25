package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;

/**
 * @author Pedro Santos
 * 
 */
public class VrapperListener extends VrapperEventAdapter {
	private static boolean vrapperEnabled;

	public VrapperListener(boolean enabled) {
		vrapperEnabled = enabled;
	}

	public static boolean isVrapperEnabled() {
		return vrapperEnabled;
	}

	@Override
	public void vrapperToggled(boolean enabled) {
		vrapperEnabled = enabled;
		BindingKeeper bindingKeeper = BindingKeeper.getDefault();
		if (bindingKeeper != null)
			bindingKeeper.setupBindings();

	}

}
