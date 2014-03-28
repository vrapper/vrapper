package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * When selecting text from InsertMode, move to VisualMode for a single
 * operation. After that operation completes, return to InsertMode. Note that
 * this class exists just to display the correct mode name in the info bar.  The
 * real magic happens in LeaveVisualModeCommand.  Every visual operation calls
 * LeaveVisualModeCommand when it's done, so that class determines whether to
 * return to NormalMode or InsertMode.
 */
public class TempVisualMode extends VisualMode {

    public static final String NAME = "temporary visual mode";
    public static final String DISPLAY_NAME = "(insert) VISUAL";

    public TempVisualMode(EditorAdaptor editorAdaptor) {
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
