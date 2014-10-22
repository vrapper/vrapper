package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.MacroRecorder;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Mode which accepts only a single keypress to know the macro name to record to.
 */
public class RecordMacroMode extends AbstractMode {

    /**
     * Start or stop macro recording mode. When starting, delegate to
     * {@link RecordMacroMode#enterMode(ModeSwitchHint...)} to initialize the mode, then wait for
     * input of the macro name.
     */
    public static final Command TOGGLE_MACRO_RECORDING = new CountIgnoringNonRepeatableCommand() {
        @Override
        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            MacroRecorder recorder = editorAdaptor.getMacroRecorder();
            if ( ! recorder.isRecording()) {
                //Go to this mode to let the user enter the macro name.
                editorAdaptor.changeModeSafely(RecordMacroMode.NAME);
            } else {
                recorder.stopRecording();
            }
        }
    };

    public static final String NAME = "record macro mode";

    public static final char TOGGLE_KEY = 'q';

    public RecordMacroMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void enterMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        super.enterMode(hints);
        //Print the toggle character in the "character buffer" area so the user knows we expect a
        //second key.
        UserInterfaceService uiService = editorAdaptor.getUserInterfaceService();
        uiService.setInfoSet(true);
        uiService.setLastCommandResultValue(String.valueOf(TOGGLE_KEY));
   }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        //Only accept printable keystrokes, everything else is simply ignored.
        if (VimConstants.PRINTABLE_KEYSTROKES.contains(stroke)) {
            editorAdaptor.getMacroRecorder().startRecording(String.valueOf(stroke.getCharacter()));
        }
        //Reset "character buffer".
        UserInterfaceService uiService = editorAdaptor.getUserInterfaceService();
        uiService.setInfoMessage("");
        uiService.setInfoSet(false);
        uiService.setLastCommandResultValue("");

        editorAdaptor.changeModeSafely(NormalMode.NAME);
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        // Cloak this sub-mode for the user.
        // Vim doesn't even print "NORMAL", so printing "MACRO" is completely out.
        return NormalMode.DISPLAY_NAME;
    }

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        // This mode can't trigger mappings.
        return null;
    }
}
