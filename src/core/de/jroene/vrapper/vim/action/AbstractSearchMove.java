package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.SearchResult;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.token.AbstractRepeatableHorizontalMove;
import de.jroene.vrapper.vim.token.Token;

public abstract class AbstractSearchMove extends AbstractRepeatableHorizontalMove {

    protected final boolean reverse;

    public AbstractSearchMove(boolean reverse) {
        super();
        this.reverse = reverse;
    }

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        Search search = getSearch(vim);
        Platform p = vim.getPlatform();
        int position = p.getPosition();
        for (int i = 0; i < times; i++) {
            position = doSearch(search, p, position);
            if (position == -1) {
                return -1;
            }
        }
        return position;
    }

    private int doSearch(Search search, Platform p, int position) {
        if (reverse) {
            search = search.reverse();
        }
        if (!search.isBackward()) {
            position += 1;
        } else if (search.getKeyword().length() == 1) {
            position -= 1;
            position = Math.max(0, position);
        }
        SearchResult result = p.find(search, position);
        if (result.isFound()) {
            return result.getIndex();
        } else {
            // redo search from beginning / end of document
            int index = search.isBackward() ? p.getLineInformation(p.getNumberOfLines()-1).getEndOffset()-1 : 0;
            result = p.find(search, index);
            if (result.isFound()) {
                return result.getIndex();
            }
        }
        return -1;
    }

    protected abstract Search getSearch(VimEmulator vim);

}