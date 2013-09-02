package net.sourceforge.vrapper.vim.modes;

/**
 * Set the initial contents of a command-line mode.
 */
public class InitialContentsHint implements ModeSwitchHint {
    private String contents;

    public InitialContentsHint(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }
}
