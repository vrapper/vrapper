package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public abstract class UpDownMotion extends CountAwareMotion {

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        TextContent content = editorAdaptor.getViewContent();
        int oldOffset = editorAdaptor.getPosition().getViewOffset();

        int lineNo = content.getLineInformationOfOffset(oldOffset).getNumber() + getJump() * count;
        lineNo = Math.max(lineNo, 0);
        lineNo = Math.min(lineNo, content.getNumberOfLines()-1);
        return editorAdaptor.getCursorService().stickyColumnAtViewLine(lineNo);
    }

    protected abstract int getJump();

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    public boolean updateStickyColumn() {
        return false;
    }

}