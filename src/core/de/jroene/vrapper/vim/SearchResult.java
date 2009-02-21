package de.jroene.vrapper.vim;

public class SearchResult {

    private final int index;

    public SearchResult(int index) {
        super();
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isFound() {
        return index >= 0;
    }
}
