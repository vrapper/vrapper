package net.sourceforge.vrapper.utils;

public class Search implements Reversible<Search> {

    private final String keyword;
    private final boolean backward;
    private final boolean caseSensitive;
    private final boolean regexSearch;
    private final boolean searchInSelection;
    private final SearchOffset afterSearch;

    public Search(String keyword, boolean backward, boolean caseSensitive) {
        this(keyword, backward, caseSensitive, SearchOffset.NONE, false);
    }

    public Search(String keyword, boolean backward, boolean caseSensitive, SearchOffset afterSearch, boolean useRegExp) {
        this(keyword, backward, caseSensitive, afterSearch, useRegExp, false);
    }
    public Search(String keyword, boolean backward, boolean caseSensitive,
            SearchOffset afterSearch, boolean useRegExp, boolean searchInSelection) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.caseSensitive = caseSensitive;
        this.afterSearch = afterSearch;
        this.regexSearch = useRegExp;
        this.searchInSelection = searchInSelection;
    }

    @Override
    public Search reverse() {
        return new Search(keyword, !backward, caseSensitive, afterSearch, regexSearch, searchInSelection);
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public boolean isBackward() {
        return backward;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isRegExSearch() {
        return regexSearch;
    }

    public boolean isSelectionSearch() {
        // \%V appears in search string
        return searchInSelection;
    }

    public SearchOffset getSearchOffset() {
        return afterSearch;
    }
}
