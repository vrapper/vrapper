package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ChangeCaretShapeCommand extends CountIgnoringNonRepeatableCommand {

    private final CaretType caretType;

    public ChangeCaretShapeCommand(CaretType caretType) {
        this.caretType = caretType;
    }

    public void execute(EditorAdaptor editorAdaptor) {
        editorAdaptor.getCursorService().setCaret(caretType);
    }

}
