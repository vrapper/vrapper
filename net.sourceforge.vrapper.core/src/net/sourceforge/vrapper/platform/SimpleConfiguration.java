package net.sourceforge.vrapper.platform;

public class SimpleConfiguration implements Configuration {

    private boolean autoIndent      = false;
    private boolean startOfLine     = true;
    private String newLine = NewLine.SYSTEM.nl;
    private boolean smartIndent     = true;

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#isStartOfLine()
     */
    public boolean isStartOfLine() {
        return startOfLine;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#setStartOfLine(boolean)
     */
    public void setStartOfLine(boolean startOfLine) {
        this.startOfLine = startOfLine;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#getNewLine()
     */
    public String getNewLine() {
        return newLine;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#setNewLine(java.lang.String)
     */
    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#setNewLine(net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine)
     */
    public void setNewLine(NewLine newLine) {
        this.newLine = newLine.nl;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#isAutoIndent()
     */
    public boolean isAutoIndent() {
        return autoIndent;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#setAutoIndent(boolean)
     */
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

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#isSmartIndent()
     */
    public boolean isSmartIndent() {
        return smartIndent;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.platform.Configuration#setSmartIndent(boolean)
     */
    public void setSmartIndent(boolean smartIndent) {
        this.smartIndent = smartIndent;
    }
}