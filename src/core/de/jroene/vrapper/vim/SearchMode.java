package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.action.SearchAction;

public class SearchMode extends AbstractCommandMode {

    public SearchMode(VimEmulator vim) {
        super(vim);
    }

    private static final String BACKWARD_SEARCH_CHAR = "?";

    @Override
    public void parseAndExecute(String first, String command) {
        boolean backward = first.equals(BACKWARD_SEARCH_CHAR);
        Search search = new Search(command, backward, false);
        vim.getRegisterManager().setSearch(search);
        new SearchAction(backward).execute(vim);
    }

}
