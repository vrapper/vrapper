package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

public class CommandLineMode extends AbstractCommandLineMode {

    // Cache this to prevent construction overhead.
    private static EvaluatorMapping coreCommands = CommandLineParser.coreCommands();

    public static final String DISPLAY_NAME = "COMMAND LINE";
    public static final String NAME = "command mode";
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

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return COMMANDLINE_KEYMAP_NAME;
    }

    public boolean addCommand(String commandName, Command command, boolean overwrite) {
        if (overwrite || !commands.contains(commandName)) {
            commands.add(commandName, command);
            return true;
        }
        return false;
    }
}
