package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SetMarkCommand extends CountIgnoringNonRepeatableCommand {

    public static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new SetMarkCommand(arg.getCharacter());
        }
    };

    private final String id;

    public SetMarkCommand(char id) {
        this(String.valueOf(id));
    }

    public SetMarkCommand(String id) {
        super();
        this.id = id;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.getCursorService().setMark(id, editorAdaptor.getPosition());
    }

}
