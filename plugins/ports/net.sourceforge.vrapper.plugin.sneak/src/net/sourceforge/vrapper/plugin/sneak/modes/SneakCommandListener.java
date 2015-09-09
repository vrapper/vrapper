package net.sourceforge.vrapper.plugin.sneak.modes;

import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakState;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakStateManager;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class SneakCommandListener extends VrapperEventAdapter {
    protected EditorAdaptor editorAdaptor;
    private SneakStateManager stateManager;

    public SneakCommandListener(EditorAdaptor editorAdaptor, SneakStateManager sneakStateManager) {
        this.editorAdaptor = editorAdaptor;
        this.stateManager = sneakStateManager;
    }

    @Override
    public void commandAboutToExecute(EditorMode mode) {
        stateManager.increaseCounters(editorAdaptor);
    }

    @Override
    public void commandExecuted(EditorMode mode) {
    }

    @Override
    public void stateReset(EditorMode mode, boolean recognized) {
        // Call this to disable sneak when the user pressed ESC in normal mode
        verifySneakState();
    }

    @Override
    public void modeAboutToSwitch(EditorMode currentMode, EditorMode newMode) {
    }

    @Override
    public void modeSwitched(EditorMode oldMode, EditorMode currentMode) {
    }

    @Override
    public void vrapperToggled(boolean enabled) {
        stateManager.markSneakInactive(editorAdaptor);
    }

    protected void verifySneakState() {
        SneakState sneakState = stateManager.getSneakState(editorAdaptor);

        if (sneakState.getGlobalEventCounter() != sneakState.getLastSneakCommandEventCounter()) {
            stateManager.markSneakInactive(editorAdaptor);
        }

        stateManager.increaseCounters(editorAdaptor);
    }
}
