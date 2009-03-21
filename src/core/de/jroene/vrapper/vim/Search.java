package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.token.Token;

public class Search {

    private final String keyword;
    private final boolean backward;
    private final boolean wholeWord;
    private final Token afterSearch;
    private final int searchOffset;

    public Search(String keyword, boolean backward, boolean wholeWord) {
        this(keyword, backward, wholeWord, null, 0);
    }

    public Search(String keyword, boolean backward, boolean wholeWord,
            Token afterSearch, int searchOffset) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.wholeWord = wholeWord;
        this.afterSearch = afterSearch;
        this.searchOffset = searchOffset;
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

    public Token getAfterSearch() {
        return afterSearch;
    }

    public int getSearchOffset() {
        return searchOffset;
    }
}
