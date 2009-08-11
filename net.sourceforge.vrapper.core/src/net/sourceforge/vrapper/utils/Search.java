package net.sourceforge.vrapper.utils;

public class Search {

    private final String keyword;
    private final boolean backward;
    private final boolean wholeWord;
    private final boolean caseSensitive;
    private final SearchOffset afterSearch;

    public Search(String keyword, boolean backward, boolean wholeWord, boolean caseSensitive) {
        this(keyword, backward, wholeWord, caseSensitive, SearchOffset.NONE);
    }

    public Search(String keyword, boolean backward, boolean wholeWord,
            boolean caseSensitive, SearchOffset afterSearch) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.wholeWord = wholeWord;
        this.caseSensitive = caseSensitive;
        this.afterSearch = afterSearch;
    }

    public Search reverse() {
        return new Search(keyword, !backward, wholeWord, caseSensitive);
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isBackward() {
        return backward;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public SearchOffset getSearchOffset() {
        return afterSearch;
    }
}
