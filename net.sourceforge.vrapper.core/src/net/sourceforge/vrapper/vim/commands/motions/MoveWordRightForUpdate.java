package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.isNewLineCharacter;
import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;
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
        int newOffset = offsetWithoutLastNewline(originalOffset, delegatePosition.getModelOffset(), editorAdaptor.getModelContent());
        
        return editorAdaptor.getCursorService().newPositionForModelOffset(newOffset);
    }

    
    private int offsetWithoutLastNewline(int originalOffset, int offset, TextContent content) {
        int bufferLength = min(MoveWithBounds.BUFFER_LEN, offset);
        if( bufferLength == 0 )
            return offset;
        
        //trim /\n{w}*/, but not /[^\n]{ws}*/
        //Also, do not trim /\n*/ only /\n/
        String buffer = content.getText(offset-bufferLength ,bufferLength);
        int lastBufferIndex = buffer.length()-1;
        
        int trailingWS = numTrailingWhitespaceChars(buffer, lastBufferIndex);
        int trailingNL = numTrailingNewLines(buffer, lastBufferIndex-trailingWS);
        
        if( trailingNL > 0 ) {
            int newOffset = offset - (trailingWS+1); //only move back a single newline
            if( newOffset > originalOffset ) //words only move right
                offset = newOffset;
        }
        
        return offset;
    }
    
    private int numTrailingWhitespaceChars(String buffer, int offset) {
       int numWS = 0;
       while( offset>=0 && Character.isWhitespace(buffer.charAt(offset)) && !isNewLineCharacter(buffer.charAt(offset) ) ) {
           numWS++;
           offset--;
       }
       
       return numWS;
    }
    
    private int numTrailingNewLines(String buffer, int offset) {
       int numWS = 0;
       while( offset>=0 && isNewLineCharacter(buffer.charAt(offset)) ) {
           numWS++;
           offset--;
       }
       
       return numWS;
    }
}
