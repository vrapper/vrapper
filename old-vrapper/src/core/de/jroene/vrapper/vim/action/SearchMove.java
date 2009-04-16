package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.VimEmulator;


public class SearchMove extends AbstractSearchMove {

    public SearchMove(boolean reversed) {
        super(reversed);
    }

    @Override
    protected Search getSearch(VimEmulator vim) {
        return vim.getRegisterManager().getSearch();
    }

}
