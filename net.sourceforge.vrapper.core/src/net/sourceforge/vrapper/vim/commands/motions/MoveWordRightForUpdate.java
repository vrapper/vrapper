package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/* This moves to the right by words, however it covers the special case of
 * updates at the end of a line: This will not move the cursor to the next line
 */
public class MoveWordRightForUpdate extends CountAwareMotion {
    
    public static final Motion MOVE_WORD_RIGHT_INSTANCE = new MoveWordRightForUpdate( new MoveWordRight(false) );
    public static final Motion MOVE_BIG_WORD_RIGHT_INSTANCE = new MoveWordRightForUpdate( new MoveBigWORDRight(false) );
    
    //delegate motion
    private CountAwareMotion delegate;
    
    private MoveWordRightForUpdate(){}
    
    private MoveWordRightForUpdate(CountAwareMotion delegate) {
        this.delegate = delegate;
    }
    
    public int getCount() {
        return delegate.getCount();
    }

    public BorderPolicy borderPolicy() {
        return delegate.borderPolicy();
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return delegate.stickyColumnPolicy();
    }

    public boolean isJump() {
        return delegate.isJump();
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition) throws CommandExecutionException {
        Position delegatePosition = delegate.destination(editorAdaptor,count, fromPosition);

        //take length of Windows newlines (\r\n) into account
        int newlineLength = editorAdaptor.getConfiguration().getNewLine().length();
        //differ from the delegate in that we trim the last newline where appropriate
        int originalOffset = fromPosition.getModelOffset();
        int newOffset = offsetWithoutLastNewline(newlineLength, originalOffset, delegatePosition.getModelOffset(), editorAdaptor.getModelContent());

        return editorAdaptor.getCursorService().newPositionForModelOffset(newOffset);
    }
    
    /**
     * Given a word defined in the TextContent object bounded by startingIndex and endingIndex,
     * return the endingIndex that defines the end of the word without the last newline.
     * <p>
     * This deletes a single newline followed by arbitrary whitespace.  It does not 
     * remove multiple newlines, or whitespace without newlines
     * 
     * @param startingIndex marks the beginning of the word
     * @param endingIndex marks the end of the word, which may be trimmed
     * @param content contains the buffer holding the word
     * 
     * @return the new ending offset, decremented if newlines and whitespace are present
     */
    public int offsetWithoutLastNewline(int newlineLength, int startingIndex, int endingIndex, TextContent content) {
        int bufferLength = min(MoveWithBounds.BUFFER_LEN, endingIndex);
        if( bufferLength == 0 )
            return endingIndex;
        
        //trim /\n{w}*/, but not /[^\n]{ws}*/
        //Also, do not trim /\n*/ only /\n/
        String buffer = content.getText(endingIndex-bufferLength ,bufferLength);
        int lastBufferIndex = buffer.length()-1;
        
        int trailingWS = numTrailingWhitespaceChars(buffer, lastBufferIndex);
        int trailingNL = numTrailingNewLines(buffer, lastBufferIndex-trailingWS);
        
        if( trailingNL > 0 ) {
            int newOffset = endingIndex - (trailingWS+newlineLength); //only move back a single newline
            if( newOffset > startingIndex ) //words only move right
                endingIndex = newOffset;
        }
        
        return endingIndex;
    }
    
    /** Given the characters in buffer ending at offset, return the number of rightmost whitespace
     * characters
     */
    private int numTrailingWhitespaceChars(String buffer, int endingIndex) {
       int numWS = 0;
       while( endingIndex>=0 && Character.isWhitespace(buffer.charAt(endingIndex)) && ! VimUtils.isNewLine(buffer.substring(endingIndex, endingIndex+1)) ) {
           numWS++;
           endingIndex--;
       }
       
       return numWS;
    }
    
    /** Given the characters in buffer ending at offset, return the number of rightmost newline
     * characters
     */
    private int numTrailingNewLines(String buffer, int endingIndex) {
       int numWS = 0;
       while( endingIndex>=0 && VimUtils.isNewLine(buffer.substring(endingIndex, endingIndex+1))) {
           numWS++;
           endingIndex--;
       }
       
       return numWS;
    }
}
    