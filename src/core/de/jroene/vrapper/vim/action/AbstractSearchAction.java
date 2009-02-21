package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.SearchResult;
import de.jroene.vrapper.vim.VimEmulator;

public abstract class AbstractSearchAction extends TokenAndAction {

    protected final boolean reverse;

    public AbstractSearchAction(boolean reverse) {
        super();
        this.reverse = reverse;
    }

    public void execute(VimEmulator vim) {
        Search search = getSearch(vim);
        Platform p = vim.getPlatform();
        int position = p.getPosition();
        if (reverse) {
            search = search.reverse();
        }
        if (!search.isBackward()) {
            position += 1;
        }
        SearchResult result = p.find(search, position);
        if (result.isFound()) {
            p.setPosition(result.getIndex());
        } else {
            // redo search from beginning / end of document
            int index = search.isBackward() ? p.getLineInformation(p.getNumberOfLines()-1).getEndOffset()-1 : 0;
            result = p.find(search, index);
            if (result.isFound()) {
                p.setPosition(result.getIndex());
            }
        }
    }

    protected abstract Search getSearch(VimEmulator vim);

}