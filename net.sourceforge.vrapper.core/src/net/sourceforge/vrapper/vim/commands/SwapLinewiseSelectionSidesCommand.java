package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class SwapLinewiseSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {
    
    public static final SwapLinewiseSelectionSidesCommand INSTANCE = new SwapLinewiseSelectionSidesCommand();

    private SwapLinewiseSelectionSidesCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        Selection selection = editorAdaptor.getSelection();

        Position from = selection.getFrom();
        Position to = selection.getTo();

        selection = new LineWiseSelection(editorAdaptor, to, from);

        MotionCommand.gotoAndChangeViewPort(editorAdaptor, from, StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(selection);
    }

}
