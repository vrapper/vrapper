package net.sourceforge.vrapper.utils;

public class Search {

    private final String keyword;
    private final boolean backward;
    private final boolean caseSensitive;
    private final boolean regexSearch;
    private final SearchOffset afterSearch;

    public Search(String keyword, boolean backward, boolean caseSensitive) {
        this(keyword, backward, caseSensitive, SearchOffset.NONE, false);
    }

    public Search(String keyword, boolean backward, boolean caseSensitive, SearchOffset afterSearch, boolean useRegExp) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.caseSensitive = caseSensitive;
        this.afterSearch = afterSearch;
        this.regexSearch = useRegExp;
    }

    public Search reverse() {
        return new Search(keyword, !backward, caseSensitive, afterSearch, regexSearch);
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isBackward() {
        return backward;
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
