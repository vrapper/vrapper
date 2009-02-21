package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.SearchResult;
import de.jroene.vrapper.vim.VimEmulator;

public class SearchAction extends TokenAndAction {

    private final boolean reverse;

    public SearchAction(boolean reversed) {
        super();
        this.reverse = reversed;
    }

    public void execute(VimEmulator vim) {
        Search search = vim.getRegisterManager().getSearch();
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

}
