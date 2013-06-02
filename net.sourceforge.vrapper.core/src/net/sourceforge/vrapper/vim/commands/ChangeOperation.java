package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.InsertMode;

class ChangeOperationRepetition implements TextOperation {

    public static final TextOperation INSTANCE = new ChangeOperationRepetition();

    @Override
    public void execute(final EditorAdaptor editorAdaptor, final int count,
            final TextObject textObject) throws CommandExecutionException {
        final Command lastInsertion = editorAdaptor.getRegisterManager().getLastInsertion();
        seq(ChangeOperation.getHintCommand(editorAdaptor, count, textObject), lastInsertion).execute(editorAdaptor);
    }

    @Override
    public TextOperation repetition() {
        return this;
    }
}

public class ChangeOperation implements TextOperation {

    public static final ChangeOperation INSTANCE = new ChangeOperation();

    private ChangeOperation() { /* NOP */ }

    @Override
    public TextOperation repetition() {
        return ChangeOperationRepetition.INSTANCE;
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) throws CommandExecutionException {
        final Command beforeInsertCmd = getHintCommand(editorAdaptor, count, textObject);
        final SelectionArea lastSel = editorAdaptor.getRegisterManager().getLastActiveSelectionArea();
        if (textObject instanceof Selection && lastSel != null
                && ContentType.TEXT_RECTANGLE.equals(lastSel.getContentType(editorAdaptor.getConfiguration()))) {
            // insert in block mode
            // the SelectionBasedTextOperationCommand already locked history for us
            
            final Command afterInsertCmd = getLeaveHintCommand(editorAdaptor, count, lastSel);
            editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(beforeInsertCmd),
                    new ExecuteCommandHint.OnLeave(afterInsertCmd));
        } else {
            // normal insert
            editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(beforeInsertCmd));
        }
    }

    static Command getHintCommand(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) {
        Command result = new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, textObject).withCount(count);
        if (ContentType.LINES.equals(textObject.getContentType(editorAdaptor.getConfiguration()))) {
            result = seq(result, InsertLineCommand.PRE_CURSOR);
        }
        return result;
    }
    
    Command getLeaveHintCommand(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) {
        return new SelectionBasedTextOperationCommand.BlockwiseRepeatCommand(this, count, true, true);
    }
    
}
