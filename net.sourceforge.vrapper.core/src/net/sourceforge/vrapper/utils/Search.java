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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (backward ? 1231 : 1237);
        result = prime * result + (caseSensitive ? 1231 : 1237);
        result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
        result = prime * result + (regexSearch ? 1231 : 1237);
        result = prime * result + (searchInSelection ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Search other = (Search) obj;
        if (backward != other.backward)
            return false;

        // Check other fields
        return equalsIgnoreDirection(other);
    }

    public boolean equalsIgnoreDirection(Search other) {
        if (other == null)
            return false;
        if (caseSensitive != other.caseSensitive)
            return false;
        if (keyword == null) {
            if (other.keyword != null)
                return false;
        } else if (!keyword.equals(other.keyword))
            return false;
        if (regexSearch != other.regexSearch)
            return false;
        if (searchInSelection != other.searchInSelection)
            return false;
        return true;
    }
}
