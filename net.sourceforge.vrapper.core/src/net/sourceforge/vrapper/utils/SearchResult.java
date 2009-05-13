package net.sourceforge.vrapper.utils;

public class SearchResult {

    private final Position index;

    public SearchResult(Position index) {
        super();
        this.index = index;
    }

    public Position getIndex() {
        return index;
    }

    public boolean isFound() {
        return index != null;
    }
}
