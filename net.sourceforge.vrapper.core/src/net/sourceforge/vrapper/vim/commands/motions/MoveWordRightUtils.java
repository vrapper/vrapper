package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.min;
import static net.sourceforge.vrapper.vim.commands.Utils.isNewLineCharacter;
import net.sourceforge.vrapper.platform.TextContent;

public class MoveWordRightUtils {

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
    public static int offsetWithoutLastNewline(int startingIndex, int endingIndex, TextContent content) {
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
            int newOffset = endingIndex - (trailingWS+1); //only move back a single newline
            if( newOffset > startingIndex ) //words only move right
                endingIndex = newOffset;
        }
        
        return endingIndex;
    }
    
    /** Given the characters in buffer ending at offset, return the number of rightmost whitespace
     * characters
     */
    private static int numTrailingWhitespaceChars(String buffer, int endingIndex) {
       int numWS = 0;
       while( endingIndex>=0 && Character.isWhitespace(buffer.charAt(endingIndex)) && !isNewLineCharacter(buffer.charAt(endingIndex) ) ) {
           numWS++;
           endingIndex--;
       }
       
       return numWS;
    }
    
    /** Given the characters in buffer ending at offset, return the number of rightmost newline
     * characters
     */
    private static int numTrailingNewLines(String buffer, int endingIndex) {
       int numWS = 0;
       while( endingIndex>=0 && isNewLineCharacter(buffer.charAt(endingIndex)) ) {
           numWS++;
           endingIndex--;
       }
       
       return numWS;
    }
}
