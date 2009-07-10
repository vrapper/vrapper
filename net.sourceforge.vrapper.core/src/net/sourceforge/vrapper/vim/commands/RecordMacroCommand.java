package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.Remapping;
import net.sourceforge.vrapper.keymap.SimpleRemapping;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.MacroRecorder;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Starts/stops recording of a macro.
 *
 * @author Matthias Radig
 */
public class RecordMacroCommand extends CountIgnoringNonRepeatableCommand {

    public static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new RecordMacroCommand(arg.getCharacter());
        }
    };

    private static final Iterable<KeyStroke> Q = ConstructorWrappers.parseKeyStrokes("q");
    private static final Remapping QA = new SimpleRemapping(ConstructorWrappers.parseKeyStrokes("qa"), false);
    private final String macroName;

    public RecordMacroCommand(char macroName) {
        this(String.valueOf(macroName));
    }

    public RecordMacroCommand(String macroName) {
        this.macroName = macroName;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        MacroRecorder recorder = editorAdaptor.getMacroRecorder();
        if (recorder.isRecording()) {
            recorder.stopRecording();
            getKeyMap(editorAdaptor).removeMapping(Q);
        } else {
            recorder.startRecording(macroName);
            // FIXME: ugly, ugly hack!
            getKeyMap(editorAdaptor).addMapping(Q, QA);
        }
    }

    private KeyMap getKeyMap(EditorAdaptor editorAdaptor) {
        return editorAdaptor.getKeyMapProvider().getKeyMap(NormalMode.KEYMAP_NAME);
    }

}
