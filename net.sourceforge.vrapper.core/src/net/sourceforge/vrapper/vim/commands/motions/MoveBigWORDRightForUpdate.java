package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class MoveBigWORDRightForUpdate extends CountAwareMotion {
    
    public static final Motion INSTANCE = new MoveBigWORDRightForUpdate(false);
    
    private CountAwareMotion delegate;
    
    private MoveBigWORDRightForUpdate() {}
    
    private MoveBigWORDRightForUpdate(boolean bailOff) {
        delegate = new MoveBigWORDRight(bailOff);
    }

    public BorderPolicy borderPolicy() {
        return delegate.borderPolicy();
    }

    public boolean updateStickyColumn() {
        return delegate.updateStickyColumn();
    }
    
    public int getCount() {
        return delegate.getCount();
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
    