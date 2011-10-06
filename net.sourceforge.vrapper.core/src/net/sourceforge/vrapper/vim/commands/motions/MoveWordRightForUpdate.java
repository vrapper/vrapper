package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/* This moves to the right by words, however it covers the special case of
 * updates at the end of a line: This will not move the cursor to the next line
 */
public class MoveWordRightForUpdate extends MoveWordRight {
    
    public static final Motion INSTANCE = new MoveWordRightForUpdate(false);
    
    protected MoveWordRightForUpdate(boolean bailOff) {
    	super(bailOff);
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) {
        int originalOffset = editorAdaptor.getPosition().getModelOffset();
        Position parentPosition = super.destination(editorAdaptor,count);
        
        //differ from the parent in that we trim the last newline where appropriate
        int newOffset = MoveWordRightUtils.offsetWithoutLastNewline(originalOffset, parentPosition.getModelOffset(), editorAdaptor.getModelContent());
        
        return editorAdaptor.getCursorService().newPositionForModelOffset(newOffset);
    }
}
    