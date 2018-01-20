package net.sourceforge.vrapper.plugin.bindingkeeper.preferences;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import net.sourceforge.vrapper.log.VrapperLog;

public class PluginPreferenceStore {
	private ScopedPreferenceStore preferenceStore;
	private String persistedConfiguration;

	public PluginPreferenceStore(ScopedPreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
		this.persistedConfiguration = getUserBindings();
	}

	public void saveUserBindings(String userBindings) {
		preferenceStore.setValue(PreferenceConstants.P_USER_BINDINGS, userBindings);

		if (persistedConfiguration != null && !persistedConfiguration.equals(userBindings))
			persist();
	}

	private void persist() {
		try {
			preferenceStore.save();
			persistedConfiguration = getUserBindings();
		} catch (IOException e) {
			VrapperLog.error("Can't save user bindings", e);
		}
	}

	public String getUserBindings() {
		return preferenceStore.getString(PreferenceConstants.P_USER_BINDINGS);
	}

	public Collection<String> getUnwantedConflicts() {
		String configuredBlacklist = preferenceStore.getString(PreferenceConstants.P_UNWANTED_CONFLICTS);
		return Arrays.asList(configuredBlacklist.split(":"));
	}

}
