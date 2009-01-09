package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.token.FindMove;
import de.jroene.vrapper.vim.token.Token;

/**
 * Contains "global" variables used by different commands.
 *
 * @author Matthias Radig
 */
public class VimVariables {

    private FindMove lastCharSearch;
    private boolean autoIndent;
    private Token lastEdit;

    public Token getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Token lastChange) {
        this.lastEdit = lastChange;
    }

    public FindMove getLastCharSearch() {
        return (FindMove) lastCharSearch.clone();
    }

    public void setLastCharSearch(FindMove findMove) {
        this.lastCharSearch = (FindMove) findMove.clone();
    }

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }
}
