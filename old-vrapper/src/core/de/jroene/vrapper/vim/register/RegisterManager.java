package de.jroene.vrapper.vim.register;

import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.token.FindMove;
import de.jroene.vrapper.vim.token.Token;

/**
 * Provides access to different registers.
 *
 * @author Matthias Radig
 */
public interface RegisterManager {

    Register getRegister(String name);
    Register getDefaultRegister();
    Register getActiveRegister();
    Register getLastEditRegister();
    void setActiveRegister(String name);
    void activateDefaultRegister();
    void activateLastEditRegister();
    Token getLastEdit();
    void setLastEdit(Token edit);
    FindMove getLastCharSearch();
    void setLastCharSearch(FindMove edit);
    Search getSearch();
    void setSearch(Search search);

}
