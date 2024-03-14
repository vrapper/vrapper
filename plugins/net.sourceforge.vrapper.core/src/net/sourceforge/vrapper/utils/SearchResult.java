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
    
    public String toString() {
        Position start = getStart();
        Position end = getEnd();
        if (start == null || end == null) {
            return "SearchResult(No match)";
        } else {
            return "SearchResult(M " + start.getModelOffset() + "/" + start.getViewOffset()
                    + " V - M " + end.getModelOffset() + "/" + end.getViewOffset() + " V)";
        }
    }
}
