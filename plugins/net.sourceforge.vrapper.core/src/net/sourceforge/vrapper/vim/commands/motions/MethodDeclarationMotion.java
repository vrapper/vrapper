package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * This class moves the cursor to the next method declaration within a Java-like
 * structured file.  All it's *really* doing is moving to the next '{' at a
 * depth of 2. Basically, the class declaration '{' is at depth 1 so all '{' at
 * depth 2 are methods.  That's how Vim does it.
 */
public class MethodDeclarationMotion extends AbstractModelSideMotion {
    
    public static final MethodDeclarationMotion NEXT_START = new MethodDeclarationMotion(false, true);
    public static final MethodDeclarationMotion PREV_START = new MethodDeclarationMotion(true, true);
    public static final MethodDeclarationMotion NEXT_END   = new MethodDeclarationMotion(false, false);
    public static final MethodDeclarationMotion PREV_END   = new MethodDeclarationMotion(true, false);
    
    public boolean backwards;
    public boolean methodBegin;
    
    protected MethodDeclarationMotion(boolean backwards, boolean methodBegin) {
        this.backwards = backwards;
        this.methodBegin = methodBegin;
    }
    

    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
        int dest = offset;
        while(count > 0) {
            dest = doIt(dest, content);
            count--;
        }
        return dest;
    }
    
    protected int doIt(int offset, TextContent content) {
        int depth = 0;
        int lastOpen = -1;
        int lastClose = -1;
        int testOffset = 0;
        char testChar;

        while(testOffset < content.getTextLength()) {
            testChar = content.getText(testOffset, 1).charAt(0);
            if(testChar == '{') {
                if(depth == 1) {
                    lastOpen = testOffset;
                }
                depth++;
            }
            else if(testChar == '}') {
                if(depth == 2) {
                    lastClose = testOffset;
                }
                depth--;
            }
            testOffset++;
            
            if(backwards && testOffset == offset) {
                if(lastOpen == -1)
                    lastOpen = offset;
                if(lastClose == -1)
                    lastClose = offset;
                return methodBegin ? lastOpen : lastClose;
            }
            
            if(! backwards) {
                if(methodBegin && lastOpen > offset) {
                    return lastOpen;
                }
                if( (!methodBegin) && lastClose > offset) {
                    return lastClose;
                }
            }
        }
        return offset;
    }
    
    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.INCLUSIVE;
    }

}
