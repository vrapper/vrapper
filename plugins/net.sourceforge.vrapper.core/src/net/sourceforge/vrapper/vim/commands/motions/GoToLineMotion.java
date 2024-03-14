package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class GoToLineMotion extends CountAwareMotion {

    public static Motion FIRST_LINE = new GoToLineMotion();
    public static Motion LAST_LINE = new GoToLineMotion() {
        @Override protected int defaultLineNo(TextContent content) {
            if (content.getTextLength() == 0)
                return 0;
            int lastChar = content.getTextLength() - 1;
            int number = content.getLineInformationOfOffset(lastChar).getNumber();
            if (content.getText(lastChar, 1).equals("\n"))
                ++number;
            return number;
        }
    };

    protected GoToLineMotion() { /* NOP */ }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition)
            throws CommandExecutionException {
        TextContent content = editorAdaptor.getModelContent();
        int lineNo = count == NO_COUNT_GIVEN ? defaultLineNo(content) : count - 1;
        if(lineNo > content.getNumberOfLines())
        	lineNo = content.getNumberOfLines();
        LineInformation line = null;
        try {
        	line = content.getLineInformation(lineNo);
        }
        catch(RuntimeException e) {
        	/**
        	 * If we're trying to move to the last line of the file but that
        	 * line is empty (blank line), it will throw a BadLocationException
        	 * bundled up in a RuntimeException.  If this happens, move to the
        	 * previous line and try again.  If that was the cause, everything
        	 * will work fine and you'll move to the last line.  If that wasn't
        	 * the cause... well, we'll probably throw the same Exception again
        	 * and be no worse off than if we hadn't tried.
        	 */
        	lineNo--;
        	line = content.getLineInformation(lineNo);
        }
        int offset = VimUtils.getFirstNonWhiteSpaceOffset(content, line);
        return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    }

    protected int defaultLineNo(TextContent content) {
        return 0;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    @Override
    public boolean isJump() {
        return true;
    }

}
