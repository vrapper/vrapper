package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.SearchResult;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.commandline.SearchOffset.End;
import de.jroene.vrapper.vim.commandline.SearchOffset.Line;
import de.jroene.vrapper.vim.token.AbstractRepeatableMove;
import de.jroene.vrapper.vim.token.Token;

public abstract class AbstractSearchMove extends AbstractRepeatableMove {

    protected final boolean reverse;
    private boolean includesTarget;
    private boolean lineWise;

    public AbstractSearchMove(boolean reverse) {
        super();
        this.reverse = reverse;
    }

    @Override
    public boolean includesTarget() {
        return includesTarget;
    }

    @Override
    public boolean isHorizontal() {
        return !lineWise;
    }

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        Search search = getSearch(vim);
        includesTarget = search.getSearchOffset() instanceof End;
        lineWise = search.getSearchOffset() instanceof Line;
        Platform p = vim.getPlatform();
        int position = search.getSearchOffset().unapply(vim, p.getPosition());
        for (int i = 0; i < times; i++) {
            position = doSearch(search, p, position);
            if (position == -1) {
                return -1;
            }
        }
        return search.getSearchOffset().apply(vim, position);
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