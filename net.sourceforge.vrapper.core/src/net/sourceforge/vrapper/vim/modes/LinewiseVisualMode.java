package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class LinewiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "linewise visual mode";

    public LinewiseVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void enterMode(Object... args) {
        super.enterMode(args);
        editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.LINEWISE, motions());
    }

    public String getName() {
        return NAME;
    }

}
