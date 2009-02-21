package de.jroene.vrapper.vim;

public class Search {

    private final String keyword;
    private final boolean backward;

    public Search(String keyword, boolean backward) {
        super();
        this.keyword = keyword;
        this.backward = backward;
    }

    public Search reverse() {
        return new Search(keyword, !backward);
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isBackward() {
        return backward;
    }
}
