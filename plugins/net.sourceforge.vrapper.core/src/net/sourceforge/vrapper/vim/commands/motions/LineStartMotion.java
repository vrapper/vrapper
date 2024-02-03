package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class LineStartMotion extends AbstractModelSideMotion {

    public static final LineStartMotion NON_WHITESPACE = new LineStartMotion(true);
    public static final LineStartMotion COLUMN0 = new LineStartMotion(false);

    private final boolean goToFirstNonWS;

    private LineStartMotion(boolean goToFirstNonWS) {
        this.goToFirstNonWS = goToFirstNonWS;
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.RESET_EOL;
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
