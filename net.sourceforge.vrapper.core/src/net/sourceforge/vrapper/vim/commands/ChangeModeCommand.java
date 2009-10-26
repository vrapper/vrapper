/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public class ChangeModeCommand extends CountIgnoringNonRepeatableCommand {
    private final String modeName;
    private final ModeSwitchHint[] args;

    public ChangeModeCommand(String modeName, ModeSwitchHint... args) {
        this.modeName = modeName;
        this.args = args;
    }

    public void execute(EditorAdaptor editorMode) {
        editorMode.changeMode(modeName, args);
    }

}