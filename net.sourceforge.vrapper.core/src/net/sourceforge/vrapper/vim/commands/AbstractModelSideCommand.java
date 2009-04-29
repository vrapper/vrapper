package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class AbstractModelSideCommand extends CountAwareCommand {

    /** called when this command is executed
     * @param content - text content we're operating on
     * @param offset - initial cursor position
     * @param count - count given for this command; NO_COUNT_GIVEN defaults to 1
     * @return new cursor position
     */
    protected abstract int execute(TextContent content, int offset, int count);

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN)
            count = 1;
        CursorService cursorService = editorAdaptor.getCursorService();
        int offset = cursorService.getPosition().getModelOffset();
        TextContent content = editorAdaptor.getModelContent();
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            offset = execute(content, offset, count);
            cursorService.setPosition(cursorService.newPositionForModelOffset(offset), true);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }

}
