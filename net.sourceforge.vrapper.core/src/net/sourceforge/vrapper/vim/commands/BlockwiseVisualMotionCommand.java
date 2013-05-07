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
            final Selection oldSelection) {
        final Position from = oldSelection.getFrom();
        final Position to = editorAdaptor.getPosition();
        System.out.println("extendSelection: " + from.getModelOffset() + " -> " + to.getModelOffset());
		editorAdaptor.setSelection(new BlockWiseSelection(editorAdaptor, from, to));
    }

    @Override
    protected Selection getSelection(final EditorAdaptor editorAdaptor) {
		Selection oldSelection = editorAdaptor.getSelection();
		if (oldSelection == null || (oldSelection instanceof BlockWiseSelection)) {
			final Position position = editorAdaptor.getPosition();
			oldSelection = new BlockWiseSelection(editorAdaptor, position, position);
			editorAdaptor.setSelection(oldSelection);
		}
        System.out.println("getSelection: " + oldSelection);
		return oldSelection;
    }

}
