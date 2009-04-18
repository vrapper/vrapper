package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class AbstractModelSideMotion extends CountAwareMotion {

    protected abstract int destination(int offset, TextContent content, int count);

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        int modelOffset = editorAdaptor.getCursorService().getPosition().getModelOffset();
        TextContent modelContent = editorAdaptor.getModelContent();
        int destination = destination(modelOffset, modelContent, count);
        return editorAdaptor.getCursorService().newPositionForModelOffset(destination);
    }

    public boolean updateStickyColumn() {
        return true;
    }

}
