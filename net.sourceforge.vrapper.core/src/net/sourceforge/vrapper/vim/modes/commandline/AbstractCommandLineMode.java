package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public abstract class AbstractCommandLineMode extends AbstractMode {
    
    protected AbstractCommandParser parser;

    protected abstract String getPrompt();

    protected abstract AbstractCommandParser createParser();
    
    public static final ModeSwitchHint FROM_VISUAL = new ModeSwitchHint() { };
    private CommandLineUI commandLine;

    public AbstractCommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        parser = createParser();
    }

    /**
     * @param args arguments for entering the mode
     */
    public void enterMode(ModeSwitchHint... args) {
        isEnabled = true;
        commandLine = editorAdaptor.getCommandLine();
        parser = createParser();
        parser.setBuffer(getPrompt());
        for(ModeSwitchHint hint : args) {
        	if(hint == FROM_VISUAL) {
        	    parser.setFromVisual(true);
        		//display '<,'> to represent visual selection
        	    String buf = parser.getBuffer() + "'<,'>";
        	    parser.setBuffer(buf);
        	}
        }
        editorAdaptor.getUserInterfaceService().setCommandLine(parser.getBuffer(), parser.getPosition());
    }

    public void leaveMode(ModeSwitchHint... hints) {
        isEnabled = false;
        parser = null;
        commandLine.close();
        commandLine = null;
    }

    public boolean handleKey(KeyStroke stroke) {
        parser.type(stroke);
        String buffer = "";
        int position = 0;
        if (isEnabled) {
            buffer = parser.getBuffer();
            position = parser.getPosition();
        }
        editorAdaptor.getUserInterfaceService().setCommandLine(buffer, position);
        return true;
    }

    protected AbstractCommandParser getParser() {
        return parser;
    }

}