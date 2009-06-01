package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandParser;
import net.sourceforge.vrapper.vim.modes.commandline.SearchCommandParser;

public class SearchMode extends AbstractCommandLineMode {

    public static final String NAME = "search mode";

    private boolean forward;

    public SearchMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void enterMode(Object... args) {
        super.enterMode(args);
        forward = args[0].equals(Direction.FORWARD);
    }

    @Override
    protected AbstractCommandParser createParser() {
        return new SearchCommandParser(editorAdaptor);
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

        @Override
    protected char activationChar() {
        return forward ? '/' : '?';
    }

    public enum Direction {
        FORWARD, BACKWARD;
    }

    public String getName() {
        return NAME;
    }

}
