/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.Position;
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
        Selection sel = editorAdaptor.getSelection();
        editorAdaptor.setSelection(null);
        editorAdaptor.changeMode(NormalMode.NAME);
        // Fix off-by-one position if selection was left to right
        // (see EvilCaret.java for details)
        if (sel != null && sel.getStart().getModelOffset() < sel.getEnd().getModelOffset()) {
            CursorService service = editorAdaptor.getCursorService();
            Position position = service.getPosition().addModelOffset(-1);
            service.setPosition(position, true);
        }
    }
}