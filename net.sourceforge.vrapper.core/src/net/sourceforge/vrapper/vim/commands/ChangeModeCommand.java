/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ChangeModeCommand extends CountIgnoringNonRepeatableCommand {
    private final String modeName;
    private final Object[] args;

    public ChangeModeCommand(String modeName, Object... args) {
        this.modeName = modeName;
        this.args = args;
    }

    public void execute(EditorAdaptor editorMode) {
        editorMode.changeMode(modeName, args);
    }

}