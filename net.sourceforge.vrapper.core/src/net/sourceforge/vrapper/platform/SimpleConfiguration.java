package net.sourceforge.vrapper.platform;

import java.util.HashMap;
import java.util.Map;

public class SimpleConfiguration implements Configuration {

    private String newLine = NewLine.SYSTEM.nl;
    private final Map<Option<?>, Object> vars = new HashMap<Option<?>, Object>();


    @SuppressWarnings("unchecked")
    public <T> T get(Option<T> key) {
        if (vars.containsKey(key)) {
            return (T) vars.get(key);
        }
        return key.getDefaultValue();
    }

    public <T> void set(Option<T> key, T value) {
        if (value == null) {
            throw new NullPointerException("value must not be null");
        }
        vars.put(key, value);
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

    public static enum NewLine {
        MAC("\r"), UNIX("\n"), WINDOWS("\r\n"), SYSTEM(System
                .getProperty("line.separator")), UNKNOWN("\n");
        public final String nl;
        private NewLine(String nl) {
            this.nl = nl;
        }
        public static NewLine parse(String nl) {
            if (nl.startsWith(WINDOWS.nl)) {
                return WINDOWS;
            } else if (nl.startsWith(UNIX.nl)) {
                return UNIX;
            } else if (nl.startsWith(MAC.nl)) {
                return UNIX;
            }
            throw new IllegalArgumentException("string does not begin with a known newline");
        }
    }
}