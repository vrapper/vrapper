package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class LineStartMotion extends AbstractModelSideMotion {

    private final boolean goToFirstNonWS;

    public LineStartMotion(boolean goToFirstNonWS) {
        this.goToFirstNonWS = goToFirstNonWS;
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    protected int destination(int position, TextContent content, int count) {
        // note: it ignores count, because that's what Vim's '^' and '0' motions do
        // (well, '^' does, try to do counted '0' ;-])
        LineInformation lineInfo = content.getLineInformationOfOffset(position);
        int result = lineInfo.getBeginOffset();
        if (goToFirstNonWS) {
            int indent = VimUtils.getIndent(content, lineInfo).length();
            result += indent;
        }
        return result;
    }

}
