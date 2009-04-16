package de.jroene.vrapper.vim;

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
