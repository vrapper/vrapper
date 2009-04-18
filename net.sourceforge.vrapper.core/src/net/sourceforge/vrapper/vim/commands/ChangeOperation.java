package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.editText;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;

public class ChangeOperation implements TextOperation {

    public void execute(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
        // Will get unlocked and finished when insert mode is left
        // XXX: this is a little fragile, but probably there is no better way of doing it
        InsertMode.inChange = true; // FIXME: this is test only
        editorAdaptor.getHistory().beginCompoundChange();
        editorAdaptor.getHistory().lock();

        DeleteOperation.doIt(editorAdaptor, range, contentType);
        editorAdaptor.changeMode(InsertMode.NAME);
        if (contentType == ContentType.LINES) {
            editText("smartEnterInverse").execute(editorAdaptor); // FIXME: user Vrapper's code
        }
    }

    public TextOperation repetition() {
        return new ChangeToLastEditOperation();
    }
}
