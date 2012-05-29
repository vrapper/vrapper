package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

public class CommandLineMode extends AbstractCommandLineMode {

    public static final String DISPLAY_NAME = "COMMAND LINE";
    public static final String NAME = "command mode";
    public static final String KEYMAP_NAME = "Command Mode Keymap";
    private CommandLineHistory history = new CommandLineHistory();

    public CommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    protected CommandLineParser createParser() {
        return new CommandLineParser(editorAdaptor, history);
    }

    @Override
    protected char activationChar() {
        return ':';
    }

    public String getName() {
        return NAME;
    }
    
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
    }

    public boolean addCommand(String commandName, Command command, boolean overwrite) {
        return ((CommandLineParser) getParser()).addCommand(commandName, command, overwrite);
    }
}
