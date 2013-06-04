package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.vim.commands.Utils.characterType;

import java.util.HashMap;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

/**
 * Base class for modes which parse strings given by the user.<br>
 * Shows the input in the {@link Platform}'s command line.
 *
 * @author Matthias Radig
 */
public abstract class AbstractCommandParser {

    protected static final KeyStroke KEY_RETURN  = key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE  = key(SpecialKey.ESC);
    protected static final KeyStroke KEY_CTRL_C  = ctrlKey('c');
    protected static final KeyStroke KEY_CTRL_W  = ctrlKey('w');
    protected static final KeyStroke KEY_CTRL_R  = ctrlKey('r');
    protected static final KeyStroke KEY_CTRL_U  = ctrlKey('u');
    protected static final KeyStroke KEY_CTRL_V  = ctrlKey('v');
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
    protected final StringBuffer buffer;
    protected final EditorAdaptor editor;
    private final CommandLineHistory history = CommandLineHistory.INSTANCE;
    private final FilePathTabCompletion tabComplete;
    private boolean modified;
    private int position;
    private boolean isFromVisual = false;
    protected boolean pasteRegister = false;
    private boolean isCommandLineHistoryEnabled = true;
    
    private interface KeyHandler {
        public void handleKey();
    }
    private HashMap<KeyStroke, KeyHandler> editMap = new HashMap<KeyStroke, KeyHandler>();
    {
        editMap.put(KEY_UP, new KeyHandler() { public void handleKey() {
            if (modified)
                history.setTemp(getCommand());
            String previous = history.getPrevious();
            setCommandFromHistory(previous);
        }});
        editMap.put(KEY_DOWN, new KeyHandler() { public void handleKey() {
            if (modified)
                history.setTemp(getCommand());
            String next = history.getNext();
            setCommandFromHistory(next);
        }});
        editMap.put(KEY_BACKSP, new KeyHandler() { public void handleKey() {
            if (position > 1) {
                buffer.replace(position - 1, position, "");
                modified = true;
                position--;
            }
        }});
        editMap.put(KEY_LEFT, new KeyHandler() { public void handleKey() {
            position = position > 1 ? position - 1 : position;
        }});
        editMap.put(KEY_RIGHT, new KeyHandler() { public void handleKey() {
            position = position != buffer.length() ? position + 1 : buffer.length();
        }});
        editMap.put(KEY_DELETE, new KeyHandler() { public void handleKey() {
            buffer.replace(position, position + 1, "");
            modified = true;
        }});
        editMap.put(KEY_CTRL_R, new KeyHandler() { public void handleKey() {
            pasteRegister = true;
            buffer.insert(position, '"');
            modified = true;
        }});
        editMap.put(KEY_HOME, new KeyHandler() { public void handleKey() {
            position = 1;
        }});
        editMap.put(KEY_END, new KeyHandler() { public void handleKey() {
            position = buffer.length();
        }});
        editMap.put(KEY_CTRL_W, new KeyHandler() { public void handleKey() {
            deleteWordBack();
        }});
        editMap.put(KEY_CTRL_U, new KeyHandler() { public void handleKey() {
            buffer.replace(1, position, "");
            modified = true;
            position = 1;
        }});
    }


    public AbstractCommandParser(EditorAdaptor vim) {
        this.editor = vim;
        this.tabComplete = new FilePathTabCompletion(this.editor);
        buffer = new StringBuffer();
        position = 0;
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
        if (pasteRegister) {
            // remove " placed by C-R
            buffer.replace(position, position + 1, "");
            modified = true;
        }
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
                        completeArgument(e);
                        pasteRegister = false;
                        return;
                    }
                }
            }
            if (pasteRegister) {
                String text = editor.getRegisterManager().getRegister(Character.toString(e.getCharacter())).getContent().getText();
                text = VimUtils.stripLastNewline(text);
                text = text.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ');
                buffer.insert(position, text);
                position += text.length();
                pasteRegister = false;
                modified = true;
            } else {
                buffer.insert(position++, e.getCharacter());
                modified = true;
            }
        }
        if (!e.equals(KEY_CTRL_R) && pasteRegister) {
            pasteRegister = false;
        }

        if (buffer.length() == 0 || e.equals(KEY_RETURN)
               || e.equals(KEY_ESCAPE) || e.equals(KEY_CTRL_C)) {
            if (c != null)
	            editor.changeModeSafely(NormalMode.NAME, new ExecuteCommandHint.OnEnter(c));
            else
	            editor.changeModeSafely(NormalMode.NAME);
        }
    }
    
    private void completeArgument(KeyStroke e) {
        int cmdLen = 0;
        boolean paths = false;
        boolean dirsOnly = false;
        if (buffer.toString().startsWith(":e ")) {
            cmdLen = 3;
        } else {
            if(buffer.toString().startsWith(":find ") ||
                    buffer.toString().startsWith(":tabf ") ) {
                cmdLen = 6;
                paths = true;
            } else {
                if(buffer.toString().startsWith(":cd ")) {
                    cmdLen = 4;
                    dirsOnly = true;
                }
            }
        }
        if (cmdLen > 0) {
            String cmd = buffer.substring(0, cmdLen);
            String prefix = buffer.substring(cmdLen);
            prefix = tabComplete.getNextMatch(prefix, paths, dirsOnly, e.withShiftKey());
            setBuffer(cmd + prefix);
        } else {
            // user hit TAB for no reason
        }
    }

    private void setCommandFromHistory(String cmd) {
        if (cmd == null)
            return;
        modified = false;
        buffer.setLength(1);
        buffer.append(cmd);
        position = buffer.length();
    }
    
    private void deleteWordBack() {
    	int offset = buffer.length() -1;
    	char c1, c2;
    	while(offset > 0) {
    		c1 = buffer.charAt(offset -1);
    		c2 = buffer.charAt(offset);
    		//this line was stolen from MoveWordLeft because
    		//I can't call that class with arbitrary text
    		if(!Character.isWhitespace(c2) && characterType(c1) != characterType(c2)) {
    			position = offset;
    			clearBufferFromPosition();
    			return;
    		}
    		offset--;
    	}
    	//if no word boundary found, leave initial character (e.g., ':') alone
    }

    public int getPosition() {
        return position;
    }

    public String getBuffer() {
        return buffer.toString();
    }
    
    public void setBuffer(String s) {
        buffer.replace(0, buffer.length(), s);
        position = buffer.length();
        modified = true;
    }

    public void clearBufferFromPosition() {
        buffer.replace(position, buffer.length(), "");
        modified = true;
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

    private String getCommand() {
        return buffer.substring(1, buffer.length());
    }

    private Command parseAndExecute() {
        String first = buffer.substring(0,1);
        String c = getCommand();
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

}