package net.sourceforge.vrapper.vim.commands.motions;

import static java.lang.Math.max;
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
            int lastChar = max(0, content.getTextLength() - 1);
            return content.getLineInformationOfOffset(lastChar).getNumber();
        }
    };

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        TextContent content = editorAdaptor.getModelContent();
        int lineNo = count == NO_COUNT_GIVEN ? defaultLineNo(content) : count - 1;
        LineInformation line = content.getLineInformation(lineNo);
        int offset = VimUtils.getFirstNonWhiteSpaceOffset(content, line);
        return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    }

    protected int defaultLineNo(TextContent content) {
        return 0;
    }

    public boolean updateStickyColumn() {
        return true;
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    @Override
    public boolean isJump() {
        return true;
    }

}
