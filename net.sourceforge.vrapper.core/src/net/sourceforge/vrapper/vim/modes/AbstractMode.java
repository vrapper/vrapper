package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class AbstractMode implements EditorMode {

    protected final EditorAdaptor editorAdaptor;
    protected boolean isEnabled = false;

    public AbstractMode(EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

}