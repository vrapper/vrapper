package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.register.RegisterContent;

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

    static Command getHintCommand(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) throws CommandExecutionException {
        Command result = new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, textObject).withCount(count);

        //if we're in LINES mode and on the last line, delete and then recreate that last line.
        TextRange region = textObject.getRegion(editorAdaptor, count);
        ContentType contentType = textObject.getContentType(editorAdaptor.getConfiguration());
        TextContent txtContent = editorAdaptor.getModelContent();
        int position = region.getLeftBound().getModelOffset();
        int length = region.getModelLength();
        String text = txtContent.getText(position, length);

        if (contentType == ContentType.LINES && position > 0
                && (text.length() == 0 || ! VimUtils.isNewLine(text.substring(text.length()-1)))) {
            result = seq(result, InsertLineCommand.POST_CURSOR);

        //if we're in LINES mode but somewhere else in the file
        } else if (ContentType.LINES.equals(contentType)) {
            result = seq(result, InsertLineCommand.PRE_CURSOR);
        }
        return result;
    }
    
    Command getLeaveHintCommand(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) throws CommandExecutionException {
        final TextObject selection = editorAdaptor.getSelection();
        final TextRange region = selection.getRegion(editorAdaptor, count);
        final RegisterContent yankContent = BlockWiseSelection.getTextBlockContent(editorAdaptor, region);
        return new SelectionBasedTextOperationCommand.BlockwiseRepeatCommand(this, count, true, true, yankContent);
    }
    
}
