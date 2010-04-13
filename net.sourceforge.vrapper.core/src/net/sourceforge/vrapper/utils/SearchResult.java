package net.sourceforge.vrapper.utils;

public class SearchResult extends StartEndTextRange {

    public SearchResult(Position index, Position endIndex) {
        super(index, endIndex);
        if (index == null != (endIndex == null)) {
            throw new IllegalArgumentException("indices must be both null or" +
            		" both not null");
        }
    }

    public boolean isFound() {
        return getStart() != null;
    }
}
