package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.editText;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ChangeToLastEditOperation implements TextOperation {
    public void execute(EditorAdaptor editorAdapter, TextRange range, ContentType contentType) throws CommandExecutionException {
        editorAdapter.getHistory().beginCompoundChange();
        DeleteOperation.doIt(editorAdapter, range, contentType);
        if (contentType == ContentType.LINES) {
            editText("smartEnterInverse").execute(editorAdapter); // FIXME: use Vrapper's code
        }
        RepeatLastInsertCommand.doIt(editorAdapter, null, 1);
        editorAdapter.getHistory().endCompoundChange();
    }

    public TextOperation repetition() {
        return this;
    }
}
