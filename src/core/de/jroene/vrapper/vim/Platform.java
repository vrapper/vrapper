package de.jroene.vrapper.vim;

/**
 * Some platform on which the vim emulator is build upon. This is the interface
 * connecting the vim emulator to the underlying editor.
 * 
 * @author Matthias Radig
 */
public interface Platform {

    /**
     * Sets the (visible) content of the command line. The implementation
     * decides how to display it.
     * 
     * @param commandLine
     *            new value of the command line.
     */
    void setCommandLine(String commandLine);

    void setActionLine(String actionLine);

    /**
     * Set the current position in the text, i.e. where the caret is displayed.
     * 
     * @param index
     *            the new position.
     */
    void setPosition(int index);

    /**
     * @return the current position in the text, i.e. where the caret is
     *         displayed.
     */
    int getPosition();

    /**
     * @return line information of the line the caret is positioned in.
     */
    LineInformation getLineInformation();

    /**
     * @param line
     *            a line in the text, zero-based.
     * @return the line information of the specified line.
     */
    LineInformation getLineInformation(int line);

    /**
     * @param offset
     *            a position in the text, zero-based.
     * @return the line information of the line which contains the given
     *         position.
     */
    LineInformation getLineInformationOfOffset(int offset);

    /**
     * @return number of lines in the text.
     */
    int getNumberOfLines();

    /**
     * @param index
     *            start of the text to replace.
     * @param length
     *            length of the text to replace.
     * @param s
     *            the replacement.
     */
    void replace(int index, int length, String s);

    /**
     * Sets an undo mark.
     */
    void setUndoMark();

    /**
     * Retrieves a substring from the text.
     * 
     * @param index
     *            start of the substring.
     * @param length
     *            length of the substring.
     * @return the specified substring of the text.
     */
    String getText(int index, int length);

    /**
     * Notices the platform that the vim emulator switched to insert mode.
     */
    void toInsertMode();

    /**
     * Notices the platform that the vim emulator switched to normal mode.
     */
    void toNormalMode();

    /**
     * Notices the platform that the vim emulator switched to command line mode.
     */
    void toCommandLineMode();

    /**
     * Undos all changes since the last undo mark was set.
     */
    void undo();

    /**
     * Redos one undo.
     */
    void redo();

    /**
     * Saves the text if possible.
     */
    void save();

    /**
     * Shifts a block of text.
     * 
     * @param line
     *            first line of the block.
     * @param lineCount
     *            number of lines of the block.
     * @param shift
     *            number of shifts. Sign indicates the direction. Positive means
     *            right, negative left.
     */
    void shift(int line, int lineCount, int shift);

    /**
     * Tells the platform which space to use in following method calls.
     * 
     * @param space
     *            either {@link Space#MODEL} or {@link Space#VIEW}.
     */
    void setSpace(Space space);

    /**
     * Set the platforms default space. TODO: is this really necessary?
     */
    void setDefaultSpace();

    /**
     * @return the current selection or null if nothing is selected
     */
    Selection getSelection();

    /**
     * Sets the selection.
     * 
     * @param s
     *            the new selection
     */
    void setSelection(Selection s);

    /**
     * Signals that following changes until {@link #endChange()} is called are
     * to be handled as a single change.
     */
    void beginChange();

    /**
     * Signals the end of a change.
     */
    void endChange();

    /**
     * Searches for the a keyword.
     * 
     * @param search
     *            the parameters of the search.
     * @param offset
     *            where to start searching.
     * @return the index of the searched string.
     */
    SearchResult find(Search search, int offset);
}