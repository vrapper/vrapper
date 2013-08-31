package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class DeselectAllCommand extends CountIgnoringNonRepeatableCommand {
    
    public static DeselectAllCommand INSTANCE = new DeselectAllCommand();
    
    private DeselectAllCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.setPosition(editorAdaptor.getSelection().getEnd(), StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(null);
    }

}
