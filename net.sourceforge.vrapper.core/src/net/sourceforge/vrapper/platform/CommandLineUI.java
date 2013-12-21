package net.sourceforge.vrapper.platform;

/**
 * Implementations of this interface will draw the command line.
 * <p>
 * The command line can be user-editable (select, cut / copy / paste) if it was created that
 * way.
 */
public interface CommandLineUI {

    public static enum CommandLineMode {
        DEFAULT,
        MESSAGE,
        REGISTER;
    }

    public void setMode(CommandLineMode mode);

    public void setPrompt(String prompt);

    public String getPrompt();

    /**
     * Resets everything after the prompt and clears the selection.
     * 
     * <p>The cursor is reset to the end. Clients are advised to use {@link #type(String)} when
     * adding text, as this will replace the current selection with the typed text.
     */
    public void resetContents(String contents);

    /** Return the position of the caret in the contents. */
    public int getPosition();

    /** Set the position of the caret in the contents - the prompt is not counted. */
    public void setPosition(int offset);

    /** Moves the caret. Negative means characters to the left, positive means to the right. */
    public void addOffsetToPosition(int offset);

    /**
     * Returns the contents of the command line excluding prompt characters.
     */
    public String getContents();

    /**
     * Returns the contents of the command line including the prompt characters.
     */
    public String getFullContents();

    /**
     * Inserts characters or replaces the current selection with new characters.
     * <p>The command line will always revert to DEFAULT mode when this method is invoked!
     */
    public void type(String characters);

    public void copySelectionToClipboard();

    public void open();

    public void close();

    /** Erases the selection or one character to the left. */
    public void erase();
    
    /** Erases the selection or one character to the right. */
    public void delete();

    /** Returns the position of the caret if placed at the end of the command line. */
    public int getEndPosition();

    /** Replaces the contents of the command line between two positions. */
    public void replace(int start, int end, String string);

    /**
     * Check if the last line of the contents is shown. If not, the content is too large and a 
     * -- More -- prompt should be shown.
     */
    public boolean isLastLineShown();

    /**
     * Scroll the contents of the command line down so they can be all brought into view.
     * @param wholeScreen If <tt>false</tt>, scrolls a single line. If true, scrolls down a whole
     *   screen at a time.
     */
    public void scrollDown(boolean wholeScreen);

}
