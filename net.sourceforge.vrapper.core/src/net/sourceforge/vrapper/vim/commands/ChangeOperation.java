package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;

public class ChangeOperation implements TextOperation {

    public static final ChangeOperation INSTANCE = new ChangeOperation();

    private ChangeOperation() { /* NOP */ }

    public TextOperation repetition() {
        return this;
    }

    public void execute(EditorAdaptor editorAdaptor, int count,
            TextObject textObject) throws CommandExecutionException {
        Command c = new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, textObject).withCount(count);
        if (ContentType.LINES.equals(textObject.getContentType(editorAdaptor.getConfiguration()))) {
            c = new VimCommandSequence(c, InsertLineCommand.PRE_CURSOR);
        }
        c.execute(editorAdaptor);
        editorAdaptor.changeMode(InsertMode.NAME);
    }
}
