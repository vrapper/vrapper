package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

/**
 * Wrapper command for motions in block-visual mode.
 *
 * @author Daniel Leong
 */
public class BlockwiseVisualMotionCommand extends AbstractVisualMotionCommand {

    public BlockwiseVisualMotionCommand(final Motion motion) {
        super(motion);
    }

    @Override
    protected void extendSelection(final EditorAdaptor editorAdaptor,
            final Selection oldSelection, int motionCount) {
        final Position from = oldSelection.getFrom(); // always constant
        Position to = editorAdaptor.getPosition();
        if (to.getModelOffset() >= editorAdaptor.getModelContent().getTextLength()) {
            //
            // Don't allow caret past the end of a document by moving it back
            // to the old position.
            //
            to = oldSelection.getTo();
            editorAdaptor.setPosition(to, getMotion(motionCount).stickyColumnPolicy());
        }
        editorAdaptor.setSelection(new BlockWiseSelection(editorAdaptor, from, to));
    }
}
