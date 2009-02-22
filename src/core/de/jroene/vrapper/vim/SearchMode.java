package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.action.SearchAction;
import de.jroene.vrapper.vim.action.TokenAndAction;

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
        TokenAndAction a = new SearchAction(backward);
        vim.getPlatform().setSpace(a.getSpace());
        a.execute(vim);
    }

}
