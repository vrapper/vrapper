package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.token.FindMove;

/**
 * Contains "global" variables used by different commands.
 *
 * @author Matthias Radig
 */
public class VimVariables {

    private FindMove lastCharSearch;
    private boolean autoIndent;

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
