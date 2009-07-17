package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;

public interface CursorService {
    /**
     * Set the current position in the text, i.e. where the caret is displayed.
     *
     * @param position the new position.
     * @param updateColumn should "sticky" column be updated?
     */
    void setPosition(Position position, boolean updateColumn);

    /**
     * Makes sticky column stick to end of line
     */
	void stickToEOL();

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

	// XXX: this feels a little bad;
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
}
