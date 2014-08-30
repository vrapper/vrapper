package net.sourceforge.vrapper.vim;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.vim.modes.EditorMode;

public class VrapperEventListeners {
    protected List<VrapperEventListener> listeners;
    private DefaultEditorAdaptor editorAdaptor;

    public VrapperEventListeners(DefaultEditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
        listeners = new ArrayList<VrapperEventListener>();
    }
    
    public void addEventListener(VrapperEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeEventListener(VrapperEventListener listener) {
        listeners.remove(listener);
    }

    public void fireCommandAboutToExecute() {
        EditorMode currentMode = editorAdaptor.currentMode;
        for (VrapperEventListener listener : listeners) {
            listener.commandAboutToExecute(currentMode);
        }
    }

    public void fireCommandExecuted() {
        EditorMode currentMode = editorAdaptor.currentMode;
        for (VrapperEventListener listener : listeners) {
            listener.commandExecuted(currentMode);
        }
    }
    
    public void fireStateReset(boolean recognized) {
        EditorMode currentMode = editorAdaptor.currentMode;
        for (VrapperEventListener listener : listeners) {
            listener.stateReset(currentMode, recognized);
        }
    }
    
    public void fireModeAboutToSwitch(EditorMode newMode) {
        EditorMode currentMode = editorAdaptor.currentMode;
        for (VrapperEventListener listener : listeners) {
            listener.modeAboutToSwitch(currentMode, newMode);
        }
    }
    
    public void fireModeSwitched(EditorMode oldMode) {
        EditorMode currentMode = editorAdaptor.currentMode;
        for (VrapperEventListener listener : listeners) {
            listener.modeSwitched(oldMode, currentMode);
        }
    }
    
    public void fireVrapperToggled(boolean enabled) {
        for (VrapperEventListener listener : listeners) {
            listener.vrapperToggled(enabled);
        }
    }
}
