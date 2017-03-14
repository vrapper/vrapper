package net.sourceforge.vrapper.plugin.bindingkeeper.preferences;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BindingKeeper.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_DISABLE_UNWANTED_CONFLICTS, false);
		store.setDefault(PreferenceConstants.P_UNWANTED_CONFLICTS,
				"CTRL+A:CTRL+X:CTRL+E:CTRL+Y:CTRL+D:CTRL+U:CTRL+F:CTRL+B:CTRL+P:CTRL+N:CTRL+V:CTRL+R:CTRL+H:CTRL+W:CTRL+U:CTRL+J:CTRL+M");
	}
}
