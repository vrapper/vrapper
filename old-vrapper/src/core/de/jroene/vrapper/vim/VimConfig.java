package de.jroene.vrapper.vim;


/**
 * Contains "global" variables used by different commands.
 *
 * @author Matthias Radig
 */
public class VimConfig {

    private boolean autoIndent      = false;
    private boolean startOfLine     = true;
    private String newLine = NewLine.SYSTEM.nl;
    private boolean smartIndent     = true;

    public boolean isStartOfLine() {
        return startOfLine;
    }

    public void setStartOfLine(boolean startOfLine) {
        this.startOfLine = startOfLine;
    }

    public String getNewLine() {
        return newLine;
    }

    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }

    public void setNewLine(NewLine newLine) {
        this.newLine = newLine.nl;
    }

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }

    public static enum NewLine {
        MAC("\r"), UNIX("\n"), WINDOWS("\r\n"), SYSTEM(System
                .getProperty("line.separator")), UNKNOWN("\n");
        public final String nl;
        private NewLine(String nl) {
            this.nl = nl;
        }
    }

    public boolean isSmartIndent() {
        return smartIndent;
    }
    
    public void setSmartIndent(boolean smartIndent) {
        this.smartIndent = smartIndent;
    }

}
