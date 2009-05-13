package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandParser;
import net.sourceforge.vrapper.vim.modes.commandline.SearchCommandParser;

public abstract class SearchMode extends AbstractCommandLineMode {

    public SearchMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    protected AbstractCommandParser createParser() {
        return new SearchCommandParser(editorAdaptor);
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

    public static class Backward extends SearchMode {

        public Backward(EditorAdaptor editorAdaptor) {
            super(editorAdaptor);
        }

        public static final String NAME = "Backward Search Mode";

        @Override
        protected char activationChar() {
            return '?';
        }

        public String getName() {
            return NAME;
        }
    }

    public static class Forward extends SearchMode {

        public Forward(EditorAdaptor editorAdaptor) {
            super(editorAdaptor);
        }

        public static final String NAME = "Forward Search Mode";

        @Override
        protected char activationChar() {
            return '/';
        }

        public String getName() {
            return NAME;
        }
    }

}
