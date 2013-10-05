package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/** Points to next occurrence of target character skipping
 * all the balanced pairs.
 *
 * @author Krzysiek Goj
 */
public class FindBalancedMotion extends AbstractModelSideMotion {

    protected final char target;
    protected final char pair;
    protected boolean upToTarget;
    protected boolean backwards;
    protected boolean ignoreEscape;

    public FindBalancedMotion(char target, char pair, boolean upToTarget, boolean backwards, boolean ignoreEscape) {
        this.target = target;
        this.pair = pair;
        this.upToTarget = upToTarget;
        this.backwards = backwards;
        this.ignoreEscape = ignoreEscape;
    }

    @Override
    protected int destination(int offset, TextContent content, int count)
            throws CommandExecutionException {
        int end = getEndSearchOffset(content, offset);
        int step = backwards ? -1 : 1;
        int depth = count;
        char current;
        while (backwards ? offset > end : offset < end) {
            offset += step;
            current = content.getText(offset, 1).charAt(0);
            if(current == target && !isEscaped(content, offset))
                --depth;
            else if (current == pair && !isEscaped(content, offset))
                ++depth;
            if (depth == 0)
                break;
        }
        if(offset >= content.getTextLength() || depth != 0 || content.getText(offset, 1).charAt(0) != target) {
            throw new CommandExecutionException("'" + target + "' not found");
        }
        if(!upToTarget) {
            offset -= step;
        }
        return offset;
    }
    
    //skip over escaped delimiters
    protected boolean isEscaped(TextContent content, int offset) {
        if(offset == 0 || ignoreEscape) {
            return false;
        }
        return content.getText(offset - 1, 1).charAt(0) == '\\';
    }

    protected int getEndSearchOffset(TextContent content, int offset) {
        return backwards ? 0 : content.getTextLength() - 1;
    }

    public BorderPolicy borderPolicy() {
        return backwards ? BorderPolicy.EXCLUSIVE : BorderPolicy.INCLUSIVE;
    }

}
