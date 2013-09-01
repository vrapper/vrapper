package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class SwapLinewiseSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {
    
    public static final SwapLinewiseSelectionSidesCommand INSTANCE = new SwapLinewiseSelectionSidesCommand();

    private SwapLinewiseSelectionSidesCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        // NOTE: we don't mirror (RECTANGULAR <-> LEFT_SHIFTED_RECTANGULAR) caret when 'selection' is non-exclusive
        // because LEFT_SHIFTED_RECTANGULAR would be invisible on beginnings of empty lines
        
        Selection selection = editorAdaptor.getSelection();
        
        Position start = selection.getStart();
        Position end = selection.getEnd();
        
        if (selection.isReversed())
            start = start.addModelOffset(-1);
        else
            end = end.addModelOffset(-1);
        
        selection = new LineWiseSelection(editorAdaptor, end, start);
        
        editorAdaptor.setPosition(end, StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(selection);
    }

}
