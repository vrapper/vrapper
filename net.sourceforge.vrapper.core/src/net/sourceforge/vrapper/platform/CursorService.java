package net.sourceforge.vrapper.platform;

import java.util.Set;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public interface CursorService {

    public static final String LAST_EDIT_MARK = ".";
    public static final String LAST_INSERT_MARK = "^";
    public static final String LAST_JUMP_MARK = "'";
    public static final String LAST_SELECTION_START_MARK = "<";
    public static final String LAST_SELECTION_END_MARK = ">";
    public static final String LAST_CHANGE_START = "[";
    public static final String LAST_CHANGE_END = "]";
    /**
     * Vrapper stores some of its internal state as marks. Any mark starting with this prefix should
     * not be shown to the user.
     */
    public static final String INTERNAL_MARK_PREFIX = "x-vrapper-";
    /** Stores a selection's "To" position as a mark. */
    public static final String INTERNAL_LAST_SELECT_TO_MARK = INTERNAL_MARK_PREFIX + "sel-to";
    /** Stores a selection's "From" position as a mark. */
    public static final String INTERNAL_LAST_SELECT_FROM_MARK = INTERNAL_MARK_PREFIX + "sel-from";

    /**
     * Set the current position in the text, i.e. where the caret is displayed.
     *
     * @param position the new position.
     * @param stickyColumnPolicy should "sticky" column be updated?
     */
    void setPosition(Position position, StickyColumnPolicy stickyColumnPolicy);

    /**
     * @return true if the current sticky column policy is StickyColumnPolicy.TO_EOL.
     */
    boolean shouldStickToEOL();

    /**
     * @return the current position in the text, i.e. where the caret is
     *         displayed.
     */
    Position getPosition();

    /**
     * @param lineNo - number of line (in view space) which we're interested in
     * @return offset of sticky column in that line
     */
    Position stickyColumnAtViewLine(int lineNo);
    Position stickyColumnAtModelLine(int lineNo);

    Position newPositionForViewOffset(int offset);
    Position newPositionForModelOffset(int offset);
    /**
     * Returns a new {@link Position} instance which never ends up in a newline combo.
     * See {@link #shiftPosition(Position, int, boolean)} for details.
     * <p>
     * <b>NOTE</b>: Some invalid offsets might be silently ignored, e.g. end of file or a negative
     * offsets. In that case the offset will be clipped to the content length or zero. Motions
     * should check that such a boundary is hit if they want to report this to their caller!
     *
     * @param offset int Offset for which a position needs to be returned.
     * @param original Position from which you move away.
     * @param allowPastLastChar if the cursor can be before a newline and must not be on a character.
     */
    Position newPositionForModelOffset(int offset, Position original, boolean allowPastLastChar);
    /**
     * Returns a new {@link Position} instance <code>delta</code> chars from <code>offset</code> and
     * makes sure that it never ends up in a newline combo. If <code>delta</code> is positive,
     * the returned position is after the newline. If it is negative, it can be before, but it is
     * possible shifted to the left if <code>allowPastLastChar</code> is <code>false</code>.
     * <p>
     * <b>NOTE</b>:If the delta makes us move past the text content length boundary e.g. end of file
     * or a negative offset, the position will be clipped to the content length or zero. Motions
     * should check that such a boundary is hit if they want to report this to their caller!
     *
     * @param offset int Offset where we are starting from.
     * @param delta int number of characters to move. Can be zero or negative.
     * @param allowPastLastChar if the cursor can be before a newline and must not be on a character.
     */
    Position shiftPositionForModelOffset(int offset, int delta, boolean allowPastLastChar);

    /**
     * Returns "visual" horizontal offset of the specified position. Visual
     * offset is constant regardless of character widths (think "tab"
     * characters) preceding the position.
     * @param position position to calculate the offset for
     * @return "visual" offset in implementation units.
     */
    int getVisualOffset(Position position);

    /**
     * Calculates position on the specified line at the provided horizontal
     * "visual" offset.
     * @param lineNo line number
     * @param visualOffset horizontal "visual" offset obtained from @ref getVisualOffset.
     * @return target position or @a null if there are no characters at the
     *         provided offset.
     */
    Position getPositionByVisualOffset(int lineNo, int visualOffset);

    /**
     * Calculates character length by visual width.
     */
    int visualWidthToChars(int visualWidth);

	//XXX: this feels a little bad;
	// other methods are position-related,
	// this one is presentation - related
	// split this interface or move this method
	// elsewhere (ViewPortService?) if you feel like it's a good idea
	void setCaret(CaretType caretType);

	/**
     * Sets a mark.
     *
     * @param id
     *            name of the mark
     * @param position
     *            where the mark should be set
     */
	void setMark(String id, Position position);

    /**
     * Gets the position of a mark.
     *
     * @param id
     *            name of the mark
     * @return the position of the mark or <code>null</code> if the mark has not
     *         been set
     */
    Position getMark(String id);

    Set<String> getAllMarks();

    boolean isGlobalMark(final String id);

    /**
     * Access change lists, 'g;' and 'g,'.
     */
    Position getNextChangeLocation(int count);
    Position getPrevChangeLocation(int count);

    /**
     * Registers the current location in the jump list.
     */
    void markCurrentPosition();

    /**
     * Updates the current location in the jump list just before jumping to a new location so that
     * we can return to it.
     */
    void updateLastPosition();
}
