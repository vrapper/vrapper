package net.sourceforge.vrapper.utils;

/**
 * Provides information about what lines are visible in the view.
 *
 * @author Matthias Radig
 */
public class ViewPortInformation {

    private final int topLine;
    private final int bottomLine;

    public ViewPortInformation(int topLine, int bottomLine) {
        super();
        this.topLine = topLine;
        this.bottomLine = bottomLine;
    }

    public int getTopLine() {
        return topLine;
    }
    public int getBottomLine() {
        return bottomLine;
    }
    public int getNumberOfLines() {
        return bottomLine-topLine;
    }
}
