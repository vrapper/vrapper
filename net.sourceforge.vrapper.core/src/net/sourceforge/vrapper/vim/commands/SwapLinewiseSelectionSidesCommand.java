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
        
        Position from = selection.getFrom();
        Position to = selection.getTo();
        
        selection = new LineWiseSelection(editorAdaptor, to, from);
        
        // This is more feature-rich in Vim: it actually remembers the sticky column
        // for both ends of the selection. We always reset the column.
        editorAdaptor.setPosition(to, StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(selection);
    }

}
