/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class LeaveVisualModeCommand extends CountIgnoringNonRepeatableCommand {

    public static final LeaveVisualModeCommand INSTANCE = new LeaveVisualModeCommand();

    private LeaveVisualModeCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        // FIXME: compatibility option: don't set caret offset
        doIt(editorAdaptor);
    }

    public static void doIt(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        editorAdaptor.setSelection(null);
        editorAdaptor.changeMode(NormalMode.NAME);
    }
}