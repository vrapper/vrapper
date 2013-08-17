package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public abstract class AbstractCommandLineMode extends AbstractMode {
    
    protected AbstractCommandParser parser;

    protected abstract String getPrompt();

    protected abstract AbstractCommandParser createParser();
    
    public static final ModeSwitchHint FROM_VISUAL = new ModeSwitchHint() { };

    public AbstractCommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        parser = createParser();
    }

    /**
     * @param args arguments for entering the mode
     */
    public void enterMode(ModeSwitchHint... args) {
        isEnabled = true;
        CommandLineUI commandLine = editorAdaptor.getCommandLine();
        commandLine.setPrompt(getPrompt());
        parser = createParser();
        parser.setCommandLine(commandLine);
        commandLine.open();
        for(ModeSwitchHint hint : args) {
        	if(hint == FROM_VISUAL) {
        	    parser.setFromVisual(true);
        		//display '<,'> to represent visual selection
        	    commandLine.resetContents("'<,'>");
        	    //set the '< and '> marks
        	    editorAdaptor.rememberLastActiveSelection();
        	}
        }
    }

    public void leaveMode(ModeSwitchHint... hints) {
        isEnabled = false;
        parser = null;
        editorAdaptor.getCommandLine().close();
    }

    public boolean handleKey(KeyStroke stroke) {
        parser.type(stroke);
        return true;
    }

    protected AbstractCommandParser getParser() {
        return parser;
    }

}