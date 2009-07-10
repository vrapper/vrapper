package net.sourceforge.vrapper.vim;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

/**
 * Handles recording of key strokes and storing the completed macro in the
 * register manager.
 *
 * @author Matthias Radig
 */
public class MacroRecorder {

    private boolean recording;
    private final RegisterManager registerManager;
    private final UserInterfaceService uiService;
    private String macroName;
    private List<KeyStroke> strokes;

    MacroRecorder(RegisterManager registerManager, UserInterfaceService uiService) {
        super();
        this.registerManager = registerManager;
        this.uiService = uiService;
    }

    /**
     * Starts recording of a macro with the given name.
     *
     * @throws NullPointerException if the macroname is <code>null</code>.
     * @throws IllegalStateException if another macro is recorded at the moment.
     */
    public void startRecording(String macroName) {
        if (macroName == null) {
            throw new NullPointerException("macro name must not be null");
        }
        if (recording) {
            throw new IllegalStateException("already recording");
        }
        recording = true;
        this.macroName = macroName;
        strokes = new ArrayList<KeyStroke>();
        uiService.setRecording(true);
    }

    /**
     * Stops the recording process and stores the macro in a register.
     *
     * @throws IllegalStateException if no macro is recorded.
     */
    public void stopRecording() {
        if (!recording) {
            throw new IllegalStateException("not recording");
        }
        recording = false;
        // small hack, last stroke (always 'q') ended the recording
        strokes.remove(strokes.size()-1);
        String seq = ConstructorWrappers.keyStrokesToString(strokes);
        RegisterContent content = new StringRegisterContent(ContentType.KEY_SEQUENCE, seq);
        registerManager.getRegister(macroName).setContent(content);
        strokes = null;
        macroName = null;
        uiService.setRecording(false);
    }

    /**
     * Notifies the recorder that the given key was pressed.
     */
    void handleKey(KeyStroke stroke) {
        if (recording) {
            strokes.add(stroke);
        }
    }

    /**
     * @return whether a macro is currently being recorded
     */
    public boolean isRecording() {
        return recording;
    }

}
