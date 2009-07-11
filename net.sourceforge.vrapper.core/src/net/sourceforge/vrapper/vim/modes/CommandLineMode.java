package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;

public class CommandLineMode extends AbstractCommandLineMode {

    public static final String NAME = "command mode";
    public static final String KEYMAP_NAME = "Command Mode Keymap";

    public CommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    protected CommandLineParser createParser() {
        return new CommandLineParser(editorAdaptor);
    }

    @Override
    protected char activationChar() {
        return ':';
    }

    public String getName() {
        return NAME;
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
    }

    public boolean addCommand(String commandName, Command command, boolean overwrite) {
        return ((CommandLineParser) getParser()).addCommand(commandName, command, overwrite);
    }
}
