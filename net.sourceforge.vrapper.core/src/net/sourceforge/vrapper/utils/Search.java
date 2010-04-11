package net.sourceforge.vrapper.utils;

public class Search {

    private final String keyword;
    private final boolean backward;
    private final boolean wholeWord;
    private final boolean caseSensitive;
    private final boolean regexSearch;
    private final SearchOffset afterSearch;

    public Search(String keyword, boolean backward, boolean wholeWord, boolean caseSensitive) {
        this(keyword, backward, wholeWord, caseSensitive, SearchOffset.NONE, false);
    }

    public Search(String keyword, boolean backward, boolean wholeWord,
            boolean caseSensitive, SearchOffset afterSearch, boolean useRegExp) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.wholeWord = wholeWord;
        this.caseSensitive = caseSensitive;
        this.afterSearch = afterSearch;
        this.regexSearch = useRegExp;
    }

    public Search reverse() {
        return new Search(keyword, !backward, wholeWord, caseSensitive, afterSearch, regexSearch);
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

    public boolean isRegExSearch() {
        return regexSearch;
    }

    public SearchOffset getSearchOffset() {
        return afterSearch;
    }
}
