package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class BlockwiseChangeOperation extends ChangeOperation {

    public static final BlockwiseChangeOperation INSTANCE = new BlockwiseChangeOperation();

    @Override
    public void execute(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) throws CommandExecutionException {
        final Command beforeInsertCmd = getHintCommand(editorAdaptor, count, textObject);
        // insert in block mode
        // the SelectionBasedTextOperationCommand already locked history for us
        
        final Command afterInsertCmd = getLeaveHintCommand(editorAdaptor, count);
        editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(beforeInsertCmd),
                new ExecuteCommandHint.OnLeave(afterInsertCmd));
    }

    Command getLeaveHintCommand(final EditorAdaptor editorAdaptor, final int count) throws CommandExecutionException {
        final TextObject selection = editorAdaptor.getSelection();
        final TextRange region = selection.getRegion(editorAdaptor, count);
        final RegisterContent yankContent = BlockWiseSelection.getTextBlockContent(editorAdaptor, region);
        return new SelectionBasedTextOperationCommand.BlockwiseRepeatCommand(this, count, true, true, yankContent);
    }
}
