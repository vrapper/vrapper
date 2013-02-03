package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.InsertMode;

class ChangeOperationRepetition implements TextOperation {

    public static final TextOperation INSTANCE = new ChangeOperationRepetition();

    public void execute(EditorAdaptor editorAdaptor, int count,
            TextObject textObject) throws CommandExecutionException {
        Command lastInsertion = editorAdaptor.getRegisterManager().getLastInsertion();
        seq(ChangeOperation.getHintCommand(editorAdaptor, count, textObject), lastInsertion).execute(editorAdaptor);
    }

    public TextOperation repetition() {
        return this;
    }
}

public class ChangeOperation implements TextOperation {

    public static final ChangeOperation INSTANCE = new ChangeOperation();

    private ChangeOperation() { /* NOP */ }

    public TextOperation repetition() {
        return ChangeOperationRepetition.INSTANCE;
    }

    public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject) throws CommandExecutionException {
        Command beforeInsertCmd = getHintCommand(editorAdaptor, count, textObject);
        editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(beforeInsertCmd));
    }

    static Command getHintCommand(EditorAdaptor editorAdaptor, int count, TextObject textObject) {
        Command result = new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, textObject).withCount(count);
        if (ContentType.LINES.equals(textObject.getContentType(editorAdaptor.getConfiguration()))) {
            result = seq(result, InsertLineCommand.PRE_CURSOR);
        }
        return result;
    }
}
