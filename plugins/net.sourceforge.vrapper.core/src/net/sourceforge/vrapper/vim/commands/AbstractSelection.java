package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;


public abstract class AbstractSelection implements Selection {

    @Override
    public TextObject repetition() {
        return this;
    }

    @Override
    public Selection reset(EditorAdaptor editorAdaptor, Position from, Position to) {
        boolean isSelectionInclusive = Selection.INCLUSIVE.equals(
                editorAdaptor.getConfiguration().get(Options.SELECTION));
        int docLength = editorAdaptor.getModelContent().getTextLength();
        CursorService cursorService = editorAdaptor.getCursorService();

        // Check that 'to' and 'from' are not out of bounds.
        // The case for an empty file is handled in shiftPositionForModelOffset()
        if (from.getModelOffset() >= docLength && isSelectionInclusive) {
            from = cursorService.shiftPositionForModelOffset(docLength, -1, true);
        } else if (from.getModelOffset() > docLength) {
            from = cursorService.newPositionForModelOffset(docLength);
        }
        if (to.getModelOffset() >= docLength && isSelectionInclusive) {
            to = cursorService.shiftPositionForModelOffset(docLength, -1, true);
        } else if (to.getModelOffset() > docLength) {
            to = cursorService.newPositionForModelOffset(docLength);
        }
        return this.doReset(editorAdaptor, from, to);
    }

    /**
     * Return a new Selection instance for the given 'from' and 'to' values. These have been
     * checked so that they are always inside the document.
     * This way calls to {@link #reset(EditorAdaptor, Position, Position)} are safe.
     */
    protected abstract Selection doReset(EditorAdaptor adaptor, Position from, Position to);
}
