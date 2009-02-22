package de.jroene.vrapper.vim;

public class Search {

    private final String keyword;
    private final boolean backward;
    private final boolean wholeWord;

    public Search(String keyword, boolean backward, boolean wholeWord) {
        super();
        this.keyword = keyword;
        this.backward = backward;
        this.wholeWord = wholeWord;
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
}
