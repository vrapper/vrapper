package de.jroene.vrapper.vim;

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

    public int getBeginOffset() {
        return beginOffset;
    }

    public int getLength() {
        return length;
    }

    public int getEndOffset() {
        return beginOffset+length;
    }

    public int calculateAboveEndOffset(Platform p) {
        if (number == 0) {
            return -1;
        }
        return p.getLineInformation(number-1).getEndOffset();
    }
}
