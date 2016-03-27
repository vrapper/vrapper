package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public class SimpleSelection extends AbstractSelection {

    private final TextRange range;
    private Position from;
    private Position to;

    /** 
     * Selection which remembers the positions of the caret when it was first created.
     * Can be used for inclusive selection or exclusive.
     */
    public SimpleSelection(Position selectionStartCaretPos, Position selectionEndCaretPos,
            TextRange range) {
        super();
        this.from = selectionStartCaretPos;
        this.to = selectionEndCaretPos;
        this.range = range;
    }

    /** Converts a {@link TextRange} into a proper selection with from / to corrected in case of
     * "inclusive" selection.
     */
    public SimpleSelection(CursorService cursorService, boolean isIncusive, TextRange range) {
        super();
        this.range = range;
        Position startSel = range.getStart();
        Position endSel = range.getEnd();
        this.from = startSel;
        this.to = endSel;
        if (range.getModelLength() > 0 && isIncusive) {
            // Fix from / to as caret is already included.
            int modelOffset;
            if (range.isReversed()) {
                modelOffset = startSel.getModelOffset();
                this.from = cursorService.shiftPositionForModelOffset(modelOffset, -1, true);
            } else {
                modelOffset = endSel.getModelOffset();
                this.to = cursorService.shiftPositionForModelOffset(modelOffset, -1, true);
            }
        }
    }

    /**
     * Selection with caret position after the selection or on first character. Should only be used
     * for an exclusive selection!
     */
    public SimpleSelection(TextRange range) {
        super();
        this.range = range;
        if (range != null) {
            from = range.getStart();
            to = range.getEnd();
        }
    }

    @Override
    public String getModeName() {
        return VisualMode.NAME;
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

    public TextRange getRegion(EditorAdaptor editorMode, int count)
            throws CommandExecutionException {
        return range;
    }

    public int getCount() {
        return 1;
    }

    public TextObject withCount(int count) {
        return this;
    }

    public Position getEnd() {
        return range.getEnd();
    }

    public Position getLeftBound() {
        return range.getLeftBound();
    }

    public int getModelLength() {
        return range.getModelLength();
    }

    public Position getRightBound() {
        return range.getRightBound();
    }

    public Position getStart() {
        return range.getStart();
    }

    public int getViewLength() {
        return range.getViewLength();
    }

    public boolean isReversed() {
        return range.isReversed();
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    @Override
    public Position getStartMark(EditorAdaptor defaultEditorAdaptor) {
        if (range.isReversed()) {
            return to;
        } else {
            return from;
        }
    }

    @Override
    public Position getEndMark(EditorAdaptor defaultEditorAdaptor) {
        if (range.isReversed()) {
            return from;
        } else {
            return to;
        }
    }

    public String toString() {
        String caretInfo = "";
        if (range != null && to != null && range.getEnd() != to) {
            caretInfo = " @ M" + to.getModelOffset()
                    + "/" + to.getViewOffset() + "V";
        }
        return "SimpleSelection( " + String.valueOf(range) + caretInfo + " )";
    }

    @Override
    public Selection doReset(EditorAdaptor adaptor, Position from, Position to) {
        boolean isSelectionInclusive = Selection.INCLUSIVE.equals(
                adaptor.getConfiguration().get(Options.SELECTION));

        if (isSelectionInclusive) {
            return new SimpleSelection(from, to,
                    StartEndTextRange.inclusive(adaptor.getCursorService(), from, to));
        } else {
            return new SimpleSelection(from, to, StartEndTextRange.exclusive(from, to));
        }
    }

    @Override
    public Selection syncToTextRange(EditorAdaptor adaptor, TextRange range) {
        boolean isSelectionInclusive = Selection.INCLUSIVE.equals(
                adaptor.getConfiguration().get(Options.SELECTION));
        Position newTo = adaptor.getPosition();
        // Selection might have shifted away from original 'from' position. Calculate new one.
        Position newFrom = range.getStart();
        // newTo is already adjusted for inclusive selection but 'from' might still need adjusting
        if (isSelectionInclusive && range.isReversed()) {
            CursorService cs = adaptor.getCursorService();
            newFrom = cs.shiftPositionForViewOffset(range.getStart().getViewOffset(), -1, true);
        }
        return new SimpleSelection(newFrom, newTo, range);
    }
}
