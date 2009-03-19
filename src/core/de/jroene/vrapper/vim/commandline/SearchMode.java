package de.jroene.vrapper.vim.commandline;

import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.SearchMove;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

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
        Token t = new SearchMove(backward);
        vim.getPlatform().setSpace(t.getSpace());
        try {
            t.evaluate(vim, null);
            t.getAction().execute(vim);
        } catch (TokenException e) {
            // not found, do nothing
        }
    }

}
