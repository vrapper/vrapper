package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.vim.commands.Utils.characterType;

import java.util.HashMap;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.CommandLineUI.CommandLineMode;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

/**
 * Base class for modes which parse strings given by the user.<br>
 * Shows the input in the {@link Platform}'s command line.
 *
 * @author Matthias Radig
 */
public abstract class AbstractCommandParser {

    protected static final KeyStroke KEY_RETURN  = key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE  = key(SpecialKey.ESC);
    protected static final KeyStroke KEY_CTRL_W  = ctrlKey('w');
    protected static final KeyStroke KEY_CTRL_R  = ctrlKey('r');
    protected static final KeyStroke KEY_CTRL_U  = ctrlKey('u');
    protected static final KeyStroke KEY_CTRL_V  = ctrlKey('v');
    protected static final KeyStroke KEY_CTRL_Y  = ctrlKey('y');
    protected static final KeyStroke KEY_BACKSP  = key(SpecialKey.BACKSPACE);
    protected static final KeyStroke KEY_DELETE  = key(SpecialKey.DELETE);
    protected static final KeyStroke KEY_UP      = key(SpecialKey.ARROW_UP);
    protected static final KeyStroke KEY_DOWN    = key(SpecialKey.ARROW_DOWN);
    protected static final KeyStroke KEY_RIGHT   = key(SpecialKey.ARROW_RIGHT);
    protected static final KeyStroke KEY_LEFT    = key(SpecialKey.ARROW_LEFT);
    protected static final KeyStroke KEY_HOME    = key(SpecialKey.HOME);
    protected static final KeyStroke KEY_END     = key(SpecialKey.END);
    protected static final SpecialKey KEY_TAB    = SpecialKey.TAB;
    protected static final SpecialKey KEY_INSERT = SpecialKey.INSERT;

    protected final EditorAdaptor editor;
    private boolean pasteRegister = false;
    protected CommandLineUI commandLine;
    private final CommandLineHistory history = CommandLineHistory.INSTANCE;

    /**
     * Whether the current command is modified and needs to be stored in the command history.
     * Commands restored from history will not be saved again unless edited.
     */
    private boolean modified;
    private boolean isFromVisual = false;
    private boolean isCommandLineHistoryEnabled = true;

    private interface KeyHandler {
        public void handleKey();
    }
    private HashMap<KeyStroke, KeyHandler> editMap = new HashMap<KeyStroke, KeyHandler>();
    {
        editMap.put(KEY_UP, new KeyHandler() { public void handleKey() {
            if (modified)
                history.setTemp(commandLine.getContents());
            String previous = history.getPrevious();
            setCommandFromHistory(previous);
        }});
        editMap.put(KEY_DOWN, new KeyHandler() { public void handleKey() {
            if (modified)
                history.setTemp(commandLine.getContents());
            String next = history.getNext();
            setCommandFromHistory(next);
        }});
        editMap.put(KEY_BACKSP, new KeyHandler() { public void handleKey() {
            if (commandLine.getContents().length() == 0) {
                editor.changeModeSafely(NormalMode.NAME);
            } else {
                commandLine.erase();
            }
            modified = true;
        }});
        editMap.put(KEY_LEFT, new KeyHandler() { public void handleKey() {
            commandLine.addOffsetToPosition(-1);
        }});
        editMap.put(KEY_RIGHT, new KeyHandler() { public void handleKey() {
            commandLine.addOffsetToPosition(1);
        }});
        editMap.put(KEY_DELETE, new KeyHandler() { public void handleKey() {
            commandLine.delete();
            modified = true;
        }});
        editMap.put(KEY_CTRL_R, new KeyHandler() { public void handleKey() {
            commandLine.setMode(CommandLineMode.REGISTER);
            pasteRegister = true;
            modified = true;
        }});
        editMap.put(KEY_HOME, new KeyHandler() { public void handleKey() {
            commandLine.setPosition(0);
        }});
        editMap.put(KEY_END, new KeyHandler() { public void handleKey() {
            commandLine.setPosition(commandLine.getEndPosition());
        }});
        editMap.put(KEY_CTRL_W, new KeyHandler() { public void handleKey() {
            deleteWordBack();
        }});
        editMap.put(KEY_CTRL_U, new KeyHandler() { public void handleKey() {
            commandLine.replace(0, commandLine.getPosition(), "");
            commandLine.setPosition(0);
            modified = true;
        }});
        editMap.put(KEY_CTRL_Y, new KeyHandler() { public void handleKey() {
            commandLine.copySelectionToClipboard();
        }});
    }


    public AbstractCommandParser(EditorAdaptor vim) {
        this.editor = vim;
        modified = false;
        history.setMode(editor.getCurrentModeName());
    }

    /**
     * Appends typed characters to the internal buffer. Deletes a char from the
     * buffer on press of the backspace key. Parses and executes the buffer on
     * press of the return key. Clears the buffer on press of the escape key.
     * Up/down arrows handle command line history.
     */
    public void type(KeyStroke e) {
        Command c = null;
        KeyHandler mappedHandler = editMap.get(e);
        if (mappedHandler != null) {
            mappedHandler.handleKey();
        } else {
            if (e.equals(KEY_RETURN)) {
                // Disable history if executed through a mapping (nmap X :foobar<CR>).
                if (isHistoryEnabled() && e.isVirtual()) {
                    setHistoryEnabled(false);
                }
                c = parseAndExecute();
            } else {
                if (e.equals(KEY_CTRL_V) || (e.getSpecialKey() == KEY_INSERT && e.withShiftKey())) {
                    pasteRegister = true;
                    e = new SimpleKeyStroke(DefaultRegisterManager.REGISTER_NAME_CLIPBOARD.charAt(0));
                } else {
                    if (e.getSpecialKey() == KEY_TAB) { //tab-completion for filenames
                        String completed = completeArgument(commandLine.getContents(), e);
                        if (completed != null) {
                            commandLine.resetContents(completed);
                        }
                        pasteRegister = false;
                        return;
                    }
                }
                if (e.getCharacter() != KeyStroke.SPECIAL_KEY && pasteRegister) {
                    String text = editor.getRegisterManager().getRegister(Character.toString(e.getCharacter())).getContent().getText();
                    text = VimUtils.stripLastNewline(text);
                    text = VimUtils.replaceNewLines(text, " ");
                    commandLine.type(text);
                    pasteRegister = false;
                    modified = true;
                } else if (e.getCharacter() != KeyStroke.SPECIAL_KEY) {
                    commandLine.type(Character.toString(e.getCharacter()));
                    modified = true;
                }
            }
        }
        //Exit register mode but not command line mode.
        if (pasteRegister && e.equals(KEY_ESCAPE)) {
            pasteRegister = false;
        } else if (e.equals(KEY_RETURN) || e.equals(KEY_ESCAPE)) {
            //Pressing return on an empty command line quits this mode rather than execute a command
            if (c == null) {
                editor.changeModeSafely(NormalMode.NAME);
            } else {
                editor.changeModeSafely(editor.getLastModeName(), new ExecuteCommandHint.OnEnter(c),
                        // If entered from visual mode, don't remove selection in case the 
                        // command c expects it.
                        AbstractVisualMode.KEEP_SELECTION_HINT);
            }
        }
        if (!e.equals(KEY_CTRL_R) && pasteRegister) {
            pasteRegister = false;
        }
        if ( ! pasteRegister) {
            commandLine.setMode(CommandLineMode.DEFAULT);
        }
    }

    protected String completeArgument(String commandLineContents, KeyStroke e) {
        return null;
    }

    private void setCommandFromHistory(String cmd) {
        if (cmd == null)
            return;
        modified = false;
        commandLine.resetContents(cmd);
    }

    private void deleteWordBack() {
        int offset = commandLine.getPosition();
    	//Simply backspace if we are at the start or first character
    	if (offset <= 1) {
    	    offset = 0;
    	} else {
    	    String contents = commandLine.getContents();
    	    if (offset > contents.length()) {
    	        offset = contents.length();
    	    }
    	    char c1, c2;
    	    do {
    	        offset--;
    	        if (offset > 0) {
    	            c1 = contents.charAt(offset - 1);
    	        } else {
    	            break;
    	        }
    	        c2 = contents.charAt(offset);
    	        //this line was stolen from MoveWordLeft because
    	        //I can't call that class with arbitrary text
    	    } while (Character.isWhitespace(c2) || characterType(c1) == characterType(c2));
    	}
    	commandLine.replace(offset, commandLine.getPosition(), "");
    	commandLine.setPosition(offset);
    }

    public boolean isHistoryEnabled() {
        return isCommandLineHistoryEnabled;
    }

    public void setHistoryEnabled(boolean isCommandLineHistoryEnabled) {
        this.isCommandLineHistoryEnabled = isCommandLineHistoryEnabled;
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
        String first = commandLine.getPrompt();
        String c = commandLine.getContents();
        if (isHistoryEnabled()) {
            history.append(c);
            setHistoryEnabled(false);
        }
        return parseAndExecute(first, c);
    }

    public boolean isFromVisual() {
        return isFromVisual;
    }

    public void setFromVisual(boolean isFromVisual) {
        this.isFromVisual = isFromVisual;
    }

    public void setCommandLine(CommandLineUI commandLine) {
        this.commandLine = commandLine;
        history.setTemp(commandLine.getContents());
    }

}
