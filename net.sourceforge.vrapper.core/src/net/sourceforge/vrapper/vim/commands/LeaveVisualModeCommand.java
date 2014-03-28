/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.TempVisualMode;

public class LeaveVisualModeCommand extends CountIgnoringNonRepeatableCommand {

    public static final LeaveVisualModeCommand INSTANCE = new LeaveVisualModeCommand();

    private LeaveVisualModeCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        // FIXME: compatibility option: don't set caret offset
        
        // Only save selection when this command is executed - other commands call doIt() as well.
        editorAdaptor.rememberLastActiveSelection();
        doIt(editorAdaptor);
    }

    public static void doIt(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        editorAdaptor.setSelection(null);
        if(editorAdaptor.getCurrentModeName().equals(TempVisualMode.NAME)) {
            editorAdaptor.changeMode(InsertMode.NAME);
        }
        else {
            editorAdaptor.changeMode(NormalMode.NAME);
        }
    }
}