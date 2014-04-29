package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Motion to go to the line closest to <i>count</i> percent of the file.
 * Also see <code>N%</code> in the Vim help document.
 */
public class PercentOfFileMotion extends AbstractModelSideMotion {

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    @Override
    protected int destination(int offset, TextContent content, int count)
            throws CommandExecutionException {
        if (count <= 0 || count > 100) {
            VrapperLog.error("Can't jump to given percent of file, value [" + count + "] invalid.");
            return offset;
        }
        int lines = content.getNumberOfLines();
        // See vimdoc for N% for the formula. Do -1 because Eclipse starts
        // counting lines at 0 whereas Vim starts at 1.
        int targetLine = ((count * lines + 99) / 100) - 1;
        LineInformation targetLineInfo = content.getLineInformation(targetLine);
        return VimUtils.getFirstNonWhiteSpaceOffset(content, targetLineInfo);
    }

}
