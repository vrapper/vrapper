package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.CommandWrappers.seq;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
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

    protected ChangeOperation() { /* NOP */ }

    @Override
    public TextOperation repetition() {
        return ChangeOperationRepetition.INSTANCE;
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor, final int count, final TextObject textObject) throws CommandExecutionException {
        Command beforeInsertCmd = getHintCommand(editorAdaptor, count, textObject);
        editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(beforeInsertCmd));
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
}
