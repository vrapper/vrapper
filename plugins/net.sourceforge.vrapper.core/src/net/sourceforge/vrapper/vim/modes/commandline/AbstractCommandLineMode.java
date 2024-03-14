package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.InitialContentsHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public abstract class AbstractCommandLineMode extends AbstractMode {

    public static final String COMMANDLINE_KEYMAP_NAME = "Command Mode Keymap";
    
    protected AbstractCommandParser parser;

    protected abstract String getPrompt();

    protected abstract AbstractCommandParser createParser();

    protected int mapBufferStart = -1;

    protected int mapBufferLength = -1;

    public static final ModeSwitchHint FROM_VISUAL = new ModeSwitchHint() { };

    public AbstractCommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    /**
     * @param args arguments for entering the mode
     */
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        isEnabled = true;
        mapBufferStart = -1;
        mapBufferLength = -1;
        CommandLineUI commandLine = editorAdaptor.getCommandLine();
        commandLine.setPrompt(getPrompt());
        parser = createParser();
        parser.setCommandLine(commandLine);
        commandLine.open();
        for(ModeSwitchHint hint : args) {
            if(hint == FROM_VISUAL) {
                parser.setFromVisual(true);
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

    @Override
    public void addKeyToMapBuffer(KeyStroke stroke) {
        String strokeString = ConstructorWrappers.keyStrokeToString(stroke);
        if (mapBufferStart == -1) {
            mapBufferStart = parser.commandLine.getPosition();
        }
        if (mapBufferLength == -1) {
            mapBufferLength = strokeString.length();
        } else {
            mapBufferLength += strokeString.length();
        }
        parser.commandLine.type(strokeString);
    }

    @Override
    public void cleanMapBuffer(boolean mappingSucceeded) {
        if (mapBufferStart != -1 && mapBufferLength != -1) {
            parser.commandLine.replace(mapBufferStart, mapBufferStart + mapBufferLength, "");
        }
        mapBufferStart = -1;
        mapBufferLength = -1;
    }

    /**
     * Returns the current parser, or <tt>null</tt> if command line mode is inactive.
     */
    protected AbstractCommandParser getParser() {
        return parser;
    }
}