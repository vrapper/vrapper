package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;

public class ChangeOperation implements TextOperation {

    private static final DeleteOperation DELETE = new DeleteOperation();

    public TextOperation repetition() {
        return new ChangeToLastEditOperation();
    }

    public void execute(EditorAdaptor editorAdaptor, int count,
            TextObject textObject) throws CommandExecutionException {
        Command c = new TextOperationTextObjectCommand(DELETE, textObject).withCount(count);
        if (ContentType.LINES.equals(textObject.getContentType())) {
            c = new VimCommandSequence(c, new InsertLineCommand(InsertLineCommand.Type.PRE_CURSOR));
        }
        editorAdaptor.changeMode(InsertMode.NAME, c);
    }
}
