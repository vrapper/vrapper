package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class MoveBigWORDRightForUpdate extends MoveBigWORDRight {
    
    public static final Motion INSTANCE = new MoveBigWORDRightForUpdate(false);
    
    private MoveBigWORDRightForUpdate(boolean bailOff) {
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
    