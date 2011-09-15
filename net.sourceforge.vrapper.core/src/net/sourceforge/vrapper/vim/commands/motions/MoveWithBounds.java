package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.min;
import static net.sourceforge.vrapper.vim.commands.Utils.isNewLineCharacter;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class MoveWithBounds extends CountAwareMotion {
    protected static final int BUFFER_LEN = 32;
    
    protected abstract boolean atBoundary(char c1, char c2);
    protected abstract boolean stopsAtNewlines();
    protected abstract boolean shouldStopAtLeftBoundingChar();
    protected abstract int destination(int offset, TextContent viewContent, boolean bailOff);

    private final boolean bailOff;
    
    public MoveWithBounds(boolean bailOff) {
        this.bailOff = bailOff;
    }
    
    public boolean updateStickyColumn() {
        return true;
    }
    
    public boolean trimsNewLineFromEnd() {
        return false;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN)
            count = 1;

        int offset = editorAdaptor.getPosition().getModelOffset();

        for (int i = 0; i < count; i++)
            offset = destination(offset, editorAdaptor.getModelContent(), bailOff && i == 0);
        
        if( trimsNewLineFromEnd() ) 
            offset = offsetWithoutLastNewline(offset, editorAdaptor.getModelContent());
        
        return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    }
    
    private int offsetWithoutLastNewline(int offset, TextContent content) {
        int bufferLength = min(BUFFER_LEN, offset);
        if( bufferLength == 0 )
            return offset;
        
        //trim /\n{w}*/, but not /[^\n]{ws}*/
        //Also, do not trim /\n*/ only /\n/
        String buffer = content.getText(offset-bufferLength ,bufferLength);
        int lastBufferIndex = buffer.length()-1;
        
        int trailingWS = numTrailingWhitespaceChars(buffer, lastBufferIndex);
        int trailingNL = numTrailingNewLines(buffer, lastBufferIndex-trailingWS);
        
        if( trailingNL > 0 ) {
            return offset - (trailingWS+1); //only trim last trailing newline, not all newlines
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
    