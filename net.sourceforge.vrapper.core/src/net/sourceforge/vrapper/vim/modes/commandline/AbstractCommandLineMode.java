package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.InitialContentsHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public abstract class AbstractCommandLineMode extends AbstractMode {
    
    protected AbstractCommandParser parser;

    protected abstract String getPrompt();

    protected abstract AbstractCommandParser createParser();
    
    public static final ModeSwitchHint FROM_VISUAL = new ModeSwitchHint() { };

    public AbstractCommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
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
        	else if(hint instanceof InitialContentsHint) {
        	    commandLine.resetContents( ((InitialContentsHint)hint).getContents() );
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

    /**
     * Returns the current parser, or <tt>null</tt> if command line mode is inactive.
     */
    protected AbstractCommandParser getParser() {
        return parser;
    }
}