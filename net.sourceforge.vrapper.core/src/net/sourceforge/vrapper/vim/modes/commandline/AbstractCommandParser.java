package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.vim.commands.Utils.characterType;
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
    protected static final KeyStroke KEY_CTRL_W = ctrlKey('w');
    protected static final KeyStroke KEY_INSERT = key(SpecialKey.INSERT);
    protected static final KeyStroke KEY_BACKSP = key(SpecialKey.BACKSPACE);
    protected static final KeyStroke KEY_DELETE = key(SpecialKey.DELETE);
    protected static final KeyStroke KEY_UP     = key(SpecialKey.ARROW_UP);
    protected static final KeyStroke KEY_DOWN   = key(SpecialKey.ARROW_DOWN);
    protected static final KeyStroke KEY_RIGHT  = key(SpecialKey.ARROW_RIGHT);
    protected static final KeyStroke KEY_LEFT   = key(SpecialKey.ARROW_LEFT);
    protected static final SpecialKey KEY_TAB   = SpecialKey.TAB;
    protected final StringBuffer buffer;
    protected final EditorAdaptor editor;
    private final CommandLineHistory history = CommandLineHistory.INSTANCE;
    private final FilePathTabCompletion tabComplete;
    private boolean modified;
    private int position;

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
        if (e.equals(KEY_RETURN)) {
            c = parseAndExecute();
        } else if (e.equals(KEY_BACKSP)) {
            // only delete if not trying to delete the initial command character from under a command
            if (position > 1 || buffer.length() == 1) {
                buffer.replace(position - 1, position, "");
                position--;
                modified = true;
            }
        } else if (e.equals(KEY_DELETE)) {
            if (position < buffer.length()) {
                buffer.replace(position, position+1, "");
                modified = true;
            }
        //use Shift+Insert for paste since Eclipse uses Ctrl+V
        //(Eclipse uses Shift+Insert too but I think it's acceptable to unbind that one)
        } else if (e.equals(KEY_INSERT) && e.withShiftKey()) {
            String text = editor.getRegisterManager().getRegister(
                    RegisterManager.REGISTER_NAME_CLIPBOARD).getContent().getText();
            text = text.replace('\n', ' ').replace('\r', ' ');
            buffer.append(text);
            position = buffer.length();
        } else if (e.equals(KEY_CTRL_W)) {
        	deleteWordBack();
        } else if (e.getSpecialKey() == KEY_TAB) { //tab-completion for filenames
        	String prefix = null;
        	if(buffer.toString().startsWith(":e ")) {
	        	//command starts with ":e " so filename starts at index 3
	        	prefix = buffer.substring(3);
        		prefix = tabComplete.getNextMatch(prefix, false, false, e.withShiftKey());
        		buffer.setLength(0);
        		buffer.append(":e " + prefix);
        		position = buffer.length();
        	}
        	else if(buffer.toString().startsWith(":find ") ||
        			buffer.toString().startsWith(":tabf ") ) {
	        	//command starts with ":find " so filename starts at index 6
	        	prefix = buffer.substring(6);
        		prefix = tabComplete.getNextMatch(prefix, true, false, e.withShiftKey());
        		buffer.setLength(6);
        		buffer.append(prefix);
        		position = buffer.length();
        	}
        	else if(buffer.toString().startsWith(":cd ")) {
	        	//command starts with ":cd " so filename starts at index 4
	        	prefix = buffer.substring(4);
        		prefix = tabComplete.getNextMatch(prefix, false, true, e.withShiftKey());
        		buffer.setLength(0);
        		buffer.append(":cd " + prefix);
        		position = buffer.length();
        	}
        	//else, user hit TAB for no reason
        } else if (e.equals(KEY_UP)) {
            if (modified)
                history.setTemp(getCommand());
            String previous = history.getPrevious();
            setCommandFromHistory(previous);
        } else if (e.equals(KEY_DOWN)) {
            if (modified)
                history.setTemp(getCommand());
            String next = history.getNext();
            setCommandFromHistory(next);
        } else if (e.equals(KEY_RIGHT)) {
            if (position < buffer.length())
            	position++;
        } else if (e.equals(KEY_LEFT)) {
            if (position > 1)
            	position--;
        } else {
            buffer.insert(position, e.getCharacter());
            position++;
            modified = true;
        }

        if (buffer.length() == 0 || e.equals(KEY_RETURN)
               || e.equals(KEY_ESCAPE) || e.equals(KEY_CTRL_C)) {
            if (c != null)
	            editor.changeModeSafely(NormalMode.NAME, new ExecuteCommandHint.OnEnter(c));
            else
	            editor.changeModeSafely(NormalMode.NAME);
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
    			buffer.setLength(offset);
    			position = buffer.length();
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
        String command = getCommand();
        history.append(command);
        return parseAndExecute(first, command);
    }

}