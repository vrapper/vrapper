package net.sourceforge.vrapper.utils;


/**
 * Information about a line in a text.
 *
 * @author Matthias Radig
 */
public class LineInformation {

    private final int number;
    private final int beginOffset;
    private final int length;

    public LineInformation(int number, int beginOffset, int length) {
        super();
        this.number = number;
        this.beginOffset = beginOffset;
        this.length = length;
    }

    /**
     * Returns the line absolute number.
     *
     * Starting at 0
     *
     * @return the line number
     */
    public int getNumber() {
        return number;
    }

    /** Offset of the first character on the line. */
    public int getBeginOffset() {
        return beginOffset;
    }

    /** Length of this line, <em>excluding</em> the newline delimiter. **/
    public int getLength() {
        return length;
    }

    /** Offset of the newline delimiter or possibly the last position in the editor. **/
    public int getEndOffset() {
        return beginOffset+length;
    }

    public String toString() {
        return "LineInfo: S:" + beginOffset + " E:"+ (beginOffset + length) + " L:" + length
                + " #:" + number;
    }
}
