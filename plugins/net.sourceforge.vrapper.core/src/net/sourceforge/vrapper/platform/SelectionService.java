package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.Selection;

public interface SelectionService {
    /**
     * Sets the selection.
     * On some implementations (including Eclipse) this also moves cursor to selection's end.
     * @param selection - new selection; null to deselect all
     */
    void setSelection(Selection selection);

    /**
     * @return current selection, returns zero-length selection if no selection is show in the
     * current Eclipse editor.
     */
    Selection getSelection();

    /**
     * Returns the native (exclusive) selection when no Vrapper selection is active. If there is no
     * selection at all, the current caret position is used for a zero-length selection.
     * @return either a correct TextRange or {@link #VRAPPER_SELECTION_ACTIVE} when Vrapper has its
     * own selection active.
     */
    TextRange getNativeSelection();

    /**
     * Sets the native selection to a range of text and forgets about any Vrapper selection. If the
     * range length is zero, any selection will be cleared and the cursor position will be moved
     * without modifying the sticky column.
     * @return either a correct TextRange or {@link #VRAPPER_SELECTION_ACTIVE} when Vrapper has its
     * own selection active.
     */
    void setNativeSelection(TextRange range);

    /**
     * Marker instance of type TextRange. Always has length {@link Integer#MAX_VALUE} and throws
     * {@link IllegalStateException} when asked for endpoints.
     */
    public static final TextRange VRAPPER_SELECTION_ACTIVE = new InvalidTextRange();

    public static class InvalidTextRange implements TextRange {
        private InvalidTextRange() {
        }
        public boolean isReversed() {
            return false;
        }
        public int getViewLength() {
            return Integer.MAX_VALUE;
        }
        public Position getStart() {
            throw new IllegalStateException("Vrapper selection active, use getSelection instead!");
        }
        public Position getRightBound() {
            throw new IllegalStateException("Vrapper selection active, use getSelection instead!");
        }
        public int getModelLength() {
            return Integer.MAX_VALUE;
        }
        public Position getLeftBound() {
            throw new IllegalStateException("Vrapper selection active, use getSelection instead!");
        }
        public Position getEnd() {
            throw new IllegalStateException("Vrapper selection active, use getSelection instead!");
        }
        public String toString() {
            return "(SelectionService: Vrapper selection active, use getSelection instead!)";
        }
    };
}
