package net.sourceforge.vrapper.plugin.bindingkeeper.preferences;

import java.io.IOException;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import net.sourceforge.vrapper.log.VrapperLog;

public class PluginPreferenceStore {
	private ScopedPreferenceStore preferenceStore;

	public PluginPreferenceStore(ScopedPreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
	}

	public void saveUserBindings(String storedUserBindings) {
		preferenceStore.setValue(PreferenceConstants.P_USER_BINDINGS, storedUserBindings);
		try {
			preferenceStore.save();
		} catch (IOException e) {
			VrapperLog.error("Can't save user bindings", e);
		}
	}

	public String getUserBindings() {
		return preferenceStore.getString(PreferenceConstants.P_USER_BINDINGS);
	}

	public String getUnwantedConflicts() {
		return preferenceStore.getString(PreferenceConstants.P_UNWANTED_CONFLICTS);
	}

	public void cleanUserBindings() {
		saveUserBindings("");
	}

}
