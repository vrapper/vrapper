package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
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
        final Position from = oldSelection.getFrom(); // always constant
        final Position oldTo = oldSelection.getTo(); // always constant
        Position to = editorAdaptor.getPosition();
//        System.out.println("extendSelection ? " + to.getModelOffset() 
//                + " <= " + from.getModelOffset());
        
        final TextContent text = editorAdaptor.getModelContent();
        final int fromCol = VimUtils.calculateColForPosition(text, from);
        final int toCol = VimUtils.calculateColForPosition(text, to);
        final boolean sameColumn = fromCol == toCol;
        final boolean sameRow = VimUtils.calculateLine(text, oldTo) == VimUtils.calculateLine(text, to);
        
        if (to.getModelOffset() <= from.getModelOffset()) {
            // cross back
            // TODO vertical part
                    
            // if they're the same column, we should decrement one more
            // so there's no bizarre empty column
            if (sameColumn) {
                final CursorService cs = editorAdaptor.getCursorService();
                to = cs.newPositionForModelOffset(to.getModelOffset()-1); // decrement normal
            }
            
//            System.out.println("extendSelection left " + from.getModelOffset() 
//                    + " -> " + to.getModelOffset());
    		editorAdaptor.setSelection(new BlockWiseSelection(editorAdaptor, 
    		        from, to));
        } else {
            if (!sameRow) {
                final CursorService cs = editorAdaptor.getCursorService();
                to = cs.newPositionForModelOffset(to.getModelOffset()-1); // decrement normal
            }
            
//            System.out.println("extendSelection (" + sameColumn + "/" + sameRow +"): " + oldSelection);
//            System.out.println("extendSelection right " + from.getModelOffset() + " -> " + to.getModelOffset());
    		editorAdaptor.setSelection(new BlockWiseSelection(editorAdaptor, from, to));
        }
    }

    @Override
    protected Selection getSelection(final EditorAdaptor editorAdaptor) {
		Selection oldSelection = editorAdaptor.getSelection();
		if (oldSelection == null) {
			final Position position = editorAdaptor.getPosition();
			oldSelection = new BlockWiseSelection(editorAdaptor, position, position);
			editorAdaptor.setSelection(oldSelection);
		}
		return oldSelection;
    }

}
