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
    private final int regionLength;
    private final boolean isBlankLine;

    public LineInformation(int number, int beginOffset, int length, int regionLength, boolean isBlankLine) {
        super();
        this.number = number;
        this.beginOffset = beginOffset;
        this.length = length;
        this.regionLength = regionLength;
        this.isBlankLine = isBlankLine;
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
    
    /**
     * Returns the length of the line excluding delimiter 
     */
    public int getRegionLength() {
        return regionLength;
    }

    /**
     * Returns the last offset on the line including delimiter
     */
    public int getEndOffset() {
        return beginOffset+length-1;
    }
    
    /**
     * Returns the length of the whole line including delimiter 
     */
    public int getLength() {
        return length;
    }

    public boolean isEmpty() {
        return getLength() == 0;
    }

    /**
     * Blank lines have length == 1 and contain only newline character  
     */
    public boolean isBlankLine() {
        return isBlankLine;
    }
}
