package net.sourceforge.vrapper.utils;

public class Search {

    private final String keyword;
    private final boolean backward;
    private final boolean wholeWord;
    private final SearchOffset afterSearch;

    public Search(String keyword, boolean backward, boolean wholeWord) {
        this(keyword, backward, wholeWord, SearchOffset.NONE);
    }

    public Search(String keyword, boolean backward, boolean wholeWord,
            SearchOffset afterSearch) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.wholeWord = wholeWord;
        this.afterSearch = afterSearch;
    }

    public Search reverse() {
        return new Search(keyword, !backward, wholeWord);
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

    public SearchOffset getSearchOffset() {
        return afterSearch;
    }
}
