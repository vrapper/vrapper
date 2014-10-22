package net.sourceforge.vrapper.vim.commands;

import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.keymap.vim.KeyStrokeConvertingState;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.MacroRecorder;

/**
 * Starts/stops recording of a macro.
 */
public class RecordMacroState implements State<Command> {

    protected static class RecordMacroCommand extends CountIgnoringNonRepeatableCommand {
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
            if ( ! recorder.isRecording()) {
                recorder.startRecording(macroName);
            }
        }
    }

    protected static final Command STOP_RECORDING_COMMAND = new CountIgnoringNonRepeatableCommand(){
        public void execute(EditorAdaptor editorAdaptor) {
            MacroRecorder recorder = editorAdaptor.getMacroRecorder();
            if (recorder.isRecording()) {
                recorder.stopRecording();
            }
        }
    };

    protected static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new RecordMacroCommand(arg.getCharacter());
        }
    };

    protected final State<Command> getMacroNameState;
    protected final KeyStroke toggleRecordingKeyStroke;
    protected final MacroRecorder recorder;

    public RecordMacroState(char startStroke, MacroRecorder macroRecorder,
            Set<KeyStroke> supportedMacroKeys) {
        toggleRecordingKeyStroke = new SimpleKeyStroke(startStroke);
        getMacroNameState = new KeyStrokeConvertingState<Command>(
                                        RecordMacroState.KEYSTROKE_CONVERTER, supportedMacroKeys);
        recorder = macroRecorder;
    }

    @Override
    public Transition<Command> press(KeyStroke key) {
        if (toggleRecordingKeyStroke.equals(key) && ! recorder.isRecording()) {
            // Accept next key as macro name
            return new SimpleTransition<Command>(getMacroNameState);
        }
        if (toggleRecordingKeyStroke.equals(key)) {
            return new SimpleTransition<Command>(STOP_RECORDING_COMMAND);
        }
        return null;
    }

    @Override
    public State<Command> union(State<Command> other) {
        return new UnionState<Command>(this, other);
    }
}
