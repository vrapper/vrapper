package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.vim.VrapperEventAdapter;
import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * Utility class which holds information about Vrapper's current mode so that not all platform
 * classes need the EditorAdapter injected to get just that.
 */
public class VrapperModeRecorder extends VrapperEventAdapter {

    protected EditorMode currentMode;
    protected boolean vrapperEnabled;

    public VrapperModeRecorder() {
    }

    @Override
    public void modeSwitched(EditorMode oldMode, EditorMode currentMode) {
        this.currentMode = currentMode;
    }

    @Override
    public void vrapperToggled(boolean enabled) {
        vrapperEnabled = enabled;
    }

    public EditorMode getCurrentMode() {
        return currentMode;
    }
}
