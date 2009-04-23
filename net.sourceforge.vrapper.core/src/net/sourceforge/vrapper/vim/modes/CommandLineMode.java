package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;

public class CommandLineMode extends AbstractMode {

    public static final String NAME = "command mode";
    public static final String KEYMAP_NAME = "Command Mode Keymap";

    private CommandLineParser parser;

    public CommandLineMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public void enterMode() {
        isEnabled = true;
        parser = new CommandLineParser(editorAdaptor);
        handleKey(ConstructorWrappers.key(':'));
    }

    public String getName() {
        return NAME;
    }

    public void leaveMode() {
        isEnabled = false;
        parser = null;
    }

    public boolean handleKey(KeyStroke stroke) {
        parser.type(stroke);
        String buffer = isEnabled ? parser.getBuffer() : "";
        editorAdaptor.getUserInterfaceService().setCommandLine(buffer);
        return true;
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
    }
}
