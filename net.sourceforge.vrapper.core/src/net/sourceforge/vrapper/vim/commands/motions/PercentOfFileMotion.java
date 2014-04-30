package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Motion to go to the line closest to <i>count</i> percent of the file.
 * Also see <code>N%</code> in the Vim help document.
 */
public class PercentOfFileMotion implements Motion {

    protected int count;

    public PercentOfFileMotion(int count) {
        this.count = count;
    }

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    @Override
    public Motion withCount(int count) {
        return new PercentOfFileMotion(count);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        TextContent content = editorAdaptor.getModelContent();
        if (count <= 0 || count > 100) {
            throw new CommandExecutionException("Can't jump to given percent of file, value ["
                    + count + "] invalid.");
        }
        int lines = content.getNumberOfLines();
        // See vimdoc for N% for the formula. Do -1 because Eclipse starts
        // counting lines at 0 whereas Vim starts at 1.
        int targetLine = ((count * lines + 99) / 100) - 1;
        LineInformation targetLineInfo = content.getLineInformation(targetLine);
        int offset;
        // Account for jump to last line: select the entire line.
        if (targetLine == lines) {
            
        }
        offset = VimUtils.getFirstNonWhiteSpaceOffset(content, targetLineInfo);
        return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    @Override
    public boolean isJump() {
        return true;
    }

}
