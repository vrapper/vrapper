package de.jroene.vrapper.vim;


/**
 * Contains "global" variables used by different commands.
 *
 * @author Matthias Radig
 */
public class VimConfig {

    private boolean autoIndent;

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }
}
