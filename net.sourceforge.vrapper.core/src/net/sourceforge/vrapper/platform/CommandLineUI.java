package net.sourceforge.vrapper.platform;

/**
 * Implementations of this interface will draw the command line.
 * <p>
 * The command line can be user-editable (select, cut / copy / paste) if it was created that
 * way.
 */
public interface CommandLineUI {
    public void setPrompt(String prompt);

    public void setContents(String contents);

    /**
     * Returns the contents of the textbox excluding prompt characters.
     * This can be called after closing the window.
     */
    public String getContents();

    /**
     * Returns the contents of the textbox including the prompt characters.
     * This can be called after closing the window.
     */
    public String getFullContents();

    public void append(String characters);

    public void close();
}
