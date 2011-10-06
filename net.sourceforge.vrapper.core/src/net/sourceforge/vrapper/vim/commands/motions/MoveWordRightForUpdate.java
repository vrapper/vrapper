package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/* This moves to the right by words, however it covers the special case of
 * updates at the end of a line: This will not move the cursor to the next line
 */
public class MoveWordRightForUpdate extends CountAwareMotion {
    
    public static final Motion INSTANCE = new MoveWordRightForUpdate(false);
    
    //delegate motion
    private CountAwareMotion delegate;
    
    private MoveWordRightForUpdate(){}
    
    private MoveWordRightForUpdate(boolean bailOff) {
        delegate = new MoveWordRight(bailOff);
    }

    public int getCount() {
        return delegate.getCount();
    }

    public BorderPolicy borderPolicy() {
        return delegate.borderPolicy();
    }

    public boolean updateStickyColumn() {
        return delegate.updateStickyColumn();
    }

    public boolean isJump() {
        return delegate.isJump();
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        int originalOffset = editorAdaptor.getPosition().getModelOffset();
        Position delegatePosition = delegate.destination(editorAdaptor,count);
        
        //differ from the delegate in that we trim the last newline where appropriate
        int newOffset = MoveWordRightUtils.offsetWithoutLastNewline(originalOffset, delegatePosition.getModelOffset(), editorAdaptor.getModelContent());
        
        return editorAdaptor.getCursorService().newPositionForModelOffset(newOffset);
    }
}
    