package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class AbstractMode implements EditorMode {

    protected final EditorAdaptor editorAdaptor;
    protected boolean isEnabled = false;

    public AbstractMode(EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
    }

}