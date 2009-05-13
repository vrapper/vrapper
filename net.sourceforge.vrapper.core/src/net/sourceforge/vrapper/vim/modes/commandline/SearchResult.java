package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.utils.Position;

public class SearchResult {

    private final Position position;

    public SearchResult(Position position) {
        super();
        this.position = position;
    }

    public Position getIndex() {
        return position;
    }

    public boolean isFound() {
        return position != null;
    }
}
