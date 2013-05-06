package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class BlockwiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "block visual mode";
    public static final String DISPLAY_NAME = "BLOCK VISUAL";    

    public BlockwiseVisualMode(final EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void fixSelection() {
        // TODO Auto-generated method stub

    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.BLOCKWISE, motions());
    }

}
