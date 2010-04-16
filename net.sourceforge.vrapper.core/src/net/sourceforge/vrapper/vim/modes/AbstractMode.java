package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public abstract class AbstractMode implements EditorMode {

    protected final EditorAdaptor editorAdaptor;
    protected boolean isEnabled = false;

    public AbstractMode(EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
    }

    public void enterMode(ModeSwitchHint... hints) throws CommandExecutionException {
        isEnabled = true;
    }

    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        isEnabled = false;
    }
}