/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class LeaveVisualModeCommand extends CountIgnoringNonRepeatableCommand {
    public void execute(EditorAdaptor editorAdaptor) {
        // FIXME: compatibility option: don't set caret offset
        editorAdaptor.setSelection(null);
        editorAdaptor.changeMode(NormalMode.NAME);
    }
}