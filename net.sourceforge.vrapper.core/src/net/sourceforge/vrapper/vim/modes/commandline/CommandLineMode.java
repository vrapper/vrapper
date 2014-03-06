package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

public class CommandLineMode extends AbstractCommandLineMode {

    // Cache this to prevent construction overhead.
    private static EvaluatorMapping coreCommands = CommandLineParser.coreCommands();

    public static final String DISPLAY_NAME = "COMMAND LINE";
    public static final String NAME = "command mode";
    public static final String KEYMAP_NAME = "Command Mode Keymap";
    private EvaluatorMapping commands;

    public CommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        commands = new EvaluatorMapping();
        commands.addAll(coreCommands);
    }

    @Override
    public CommandLineParser createParser() {
        return new CommandLineParser(editorAdaptor, commands);
    }

    @Override
    protected String getPrompt() {
        return ":";
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
        if (overwrite || !commands.contains(commandName)) {
            commands.add(commandName, command);
            // Only triggered during .vrapperrc sourcing. In the normal flow, createParser()
            // will be called each time and the userCommands will be injected again.
            if (getParser() != null) {
                ((CommandLineParser)getParser()).addCommand(commandName, command, overwrite);
            }
            return true;
        }
        return false;
    }
}
