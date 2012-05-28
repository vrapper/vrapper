package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.RegisterManager;

/**
 * Base class for modes which parse strings given by the user.<br>
 * Shows the input in the {@link Platform}'s command line.
 *
 * @author Matthias Radig
 */
public abstract class AbstractCommandParser {

    protected static final KeyStroke KEY_RETURN = key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE = key(SpecialKey.ESC);
    protected static final KeyStroke KEY_CTRL_C = ctrlKey('c');
    protected static final KeyStroke KEY_BACKSP = key(SpecialKey.BACKSPACE);
    protected static final KeyStroke KEY_CTRL_V = key((char) 22);
    protected final StringBuffer buffer;
    protected final EditorAdaptor editor;

    public AbstractCommandParser(EditorAdaptor vim) {
        this.editor = vim;
        buffer = new StringBuffer();
    }

    /**
     * Appends typed characters to the internal buffer. Deletes a char from the
     * buffer on press of the backspace key. Parses and executes the buffer on
     * press of the return key. Clears the buffer on press of the escape key.
     */
    public void type(KeyStroke e) {
    	Command c = null;
        if (e.equals(KEY_RETURN)) {
            c = parseAndExecute();
        } else if (e.equals(KEY_BACKSP)) {
            buffer.setLength(buffer.length()-1);
            // TODO: on Mac OS, Cmd-V should be used
        } else if (e.equals(KEY_CTRL_V)) {
            String text = editor.getRegisterManager().getRegister(
                    RegisterManager.REGISTER_NAME_CLIPBOARD).getContent().getText();
            text = text.replace('\n', ' ').replace('\r', ' ');
            buffer.append(text);
        } else {
            buffer.append(e.getCharacter());
        }

        if (buffer.length() == 0 || e.equals(KEY_RETURN)
               || e.equals(KEY_ESCAPE) || e.equals(KEY_CTRL_C)) {
            if (c != null)
	            editor.changeModeSafely(NormalMode.NAME, new ExecuteCommandHint.OnEnter(c));
            else
	            editor.changeModeSafely(NormalMode.NAME);
        }
    }

    public String getBuffer() {
        return buffer.toString();
    }

    /**
	 * Parses and executes the given command if possible.
	 * 
	 * @param first
	 *            character used to activate the mode.
	 * @param command
	 *            the command to execute.
	 * @return a command to be executed in normal mode.
	 */
    public abstract Command parseAndExecute(String first, String command);

    private Command parseAndExecute() {
        String first = buffer.substring(0,1);
        String command = buffer.substring(1, buffer.length());
        return parseAndExecute(first, command);
    }

}