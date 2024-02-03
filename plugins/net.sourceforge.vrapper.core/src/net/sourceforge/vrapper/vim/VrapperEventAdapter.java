package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * Utility class to override only the event listener methods which are interesting.
 */
public class VrapperEventAdapter implements VrapperEventListener {

    @Override
    public void commandAboutToExecute(EditorMode mode) {
    }

    @Override
    public void commandExecuted(EditorMode mode) {
    }

    @Override
    public void stateReset(EditorMode mode, boolean recognized) {
    }

    @Override
    public void modeAboutToSwitch(EditorMode currentMode, EditorMode newMode) {
    }

    @Override
    public void modeSwitched(EditorMode oldMode, EditorMode currentMode) {
    }

    @Override
    public void vrapperToggled(boolean enabled) {
    }
}
