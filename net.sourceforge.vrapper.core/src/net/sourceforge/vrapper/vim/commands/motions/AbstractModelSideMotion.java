package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public abstract class AbstractModelSideMotion extends CountAwareMotion {

    protected abstract int destination(int offset, TextContent content, int count) throws CommandExecutionException;
    protected boolean isLeftRight() {return false;}
    protected void setCurrentState(String mode, Selection sel) {}

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        int modelOffset = editorAdaptor.getCursorService().getPosition().getModelOffset();
        if (VisualMode.NAME.equals(editorAdaptor.getCurrentModeName())) {
            Selection selection = editorAdaptor.getSelection();
            if (selection != null && !isLeftRight() && selection.getStart().getModelOffset() < selection.getEnd().getModelOffset()) {
                modelOffset--;
            }
        }
        setCurrentState(editorAdaptor.getCurrentModeName(), editorAdaptor.getSelection());
        TextContent modelContent = editorAdaptor.getModelContent();
        int destination = destination(modelOffset, modelContent, count);
        return editorAdaptor.getCursorService().newPositionForModelOffset(destination);
    }

    public boolean updateStickyColumn() {
        return true;
    }

}
