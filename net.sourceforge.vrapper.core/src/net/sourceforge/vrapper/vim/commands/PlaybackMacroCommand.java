package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.MacroPlayer;
import net.sourceforge.vrapper.vim.register.RegisterContent;

/**
 * Enqueues a macro in the playlist of the {@link MacroPlayer}.
 *
 * @author Matthias Radig
 */
public class PlaybackMacroCommand extends SimpleRepeatableCommand {

    public static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new PlaybackMacroCommand(arg.getCharacter());
        }
    };

    private final String macroName;

    public PlaybackMacroCommand(char macroName) {
        this(String.valueOf(macroName));
    }

    public PlaybackMacroCommand(String macroName) {
        this.macroName = macroName;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        RegisterContent content = editorAdaptor.getRegisterManager().getRegister(macroName).getContent();
        if (content == null) {
            throw new CommandExecutionException("Macro "+macroName+" does not exist");
        }
        Iterable<KeyStroke> parsed = ConstructorWrappers.parseKeyStrokes(content.getText());
        editorAdaptor.getMacroPlayer().add(parsed);
    }

    public Command repetition() {
        return this;
    }

}
