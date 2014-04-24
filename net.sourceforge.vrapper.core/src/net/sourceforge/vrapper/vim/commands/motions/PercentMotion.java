package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Either goes to the line closest to <i>count</i> percent of the file or jumps to a matching
 * parenthese if no count is given.
 * 
 * Counts greater than hundred are ignored.
 */
public class PercentMotion extends CountAwareMotion {
    
    public static final Motion INSTANCE = new PercentMotion();

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        CursorService cursorService = editorAdaptor.getCursorService();
        if (count == NO_COUNT_GIVEN) {
            return ParenthesesMove.INSTANCE.destination(editorAdaptor, count);
        } else if (count > 100) {
            // Invalid count, ignore motion.
            return cursorService.getPosition();
        } else if (count == 100) {
            return GoToLineMotion.LAST_LINE.destination(editorAdaptor);
        } else {
            TextContent modelContent = editorAdaptor.getModelContent();
            int lines = modelContent.getNumberOfLines();
            // See vimdoc for N%
            int targetLine = (count * lines + 99) / 100;
            LineInformation targetLineInfo = modelContent.getLineInformation(targetLine);
            int targetOffset = VimUtils.getFirstNonWhiteSpaceOffset(modelContent, targetLineInfo);

            return cursorService.newPositionForModelOffset(targetOffset);
        }
    }

    @Override
    public boolean isJump() {
        // Don't clobber "last jump" mark if count is invalid
        return getCount() == NO_COUNT_GIVEN || getCount() <= 100;
    }
}
