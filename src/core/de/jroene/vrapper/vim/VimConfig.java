package de.jroene.vrapper.vim;


/**
 * Contains "global" variables used by different commands.
 *
 * @author Matthias Radig
 */
public class VimConfig {

    private boolean autoIndent;
    private NewLine newLine = NewLine.SYSTEM;

    public NewLine getNewLine() {
        return newLine;
    }

    public void setNewLine(NewLine newLine) {
        this.newLine = newLine;
    }

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }

    public static enum NewLine {
        MAC("\r"), UNIX("\n"), WINDOWS("\r\n"), SYSTEM(System.getProperty("line.separator"));

        public final String nl;
        private NewLine(String nl) {
            this.nl = nl;
        }
    }

}
