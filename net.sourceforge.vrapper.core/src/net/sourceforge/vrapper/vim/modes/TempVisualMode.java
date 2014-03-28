package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class TempVisualMode extends VisualMode {

    public static final String NAME = "temporary visual mode";
    public static final String DISPLAY_NAME = "(insert) VISUAL";

    public TempVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        //isEnabled flag is used to avoid infinite recursive calls to leaveMode.
        boolean switchBackToInsert = isEnabled;
        super.leaveMode(hints);
        if (switchBackToInsert) {
            editorAdaptor.changeModeSafely(InsertMode.NAME, InsertMode.RESUME_ON_MODE_ENTER);
        }
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
