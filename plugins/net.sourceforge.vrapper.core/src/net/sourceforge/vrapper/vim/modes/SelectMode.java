package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SelectMode extends InsertMode {

    public static final String NAME = "select mode";
    public static final String DISPLAY_NAME = "SELECT";

    public SelectMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
