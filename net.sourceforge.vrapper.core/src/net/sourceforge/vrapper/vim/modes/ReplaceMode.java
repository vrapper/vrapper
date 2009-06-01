package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ReplaceMode extends InsertMode {

    public static final String NAME = "replace mode";

    public ReplaceMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override public String getName() {
        return NAME;
    }

    @Override public void enterMode(Object... args) {
        super.enterMode(args);
        editorAdaptor.getEditorSettings().setReplaceMode(true);
        editorAdaptor.getCursorService().setCaret(CaretType.UNDERLINE);
    }

    @Override public void leaveMode() {
        editorAdaptor.getEditorSettings().setReplaceMode(false);
        super.leaveMode();
    }

}
