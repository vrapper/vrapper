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
     * @return the current state of the platform's viewport.
     */
    ViewPortInformation getViewPortInformation();

    /**
     * Sets the first line of the view port.
     * 
     * @param number
     *            the number of the smallest line which should be visible.
     */
    void setTopLine(int number);

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
     * Inserts text at caret's position.
     * 
     * @param s the text which gets inserted
     */
    void insert(String s);

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
     * Notices the platform that the vim emulator switched to visual mode.
     */
    void toVisualMode();

    /**
     * Notices the platform that there is now an operator pending in normal mode.
     */
    void toOperatorPendingMode();

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
     * Controls whether the editor component will be repainted.
     * 
     * @param repaint
     *            whether repaints will be done or not.
     */
    void setRepaint(boolean repaint);

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

    /**
     * Tells the platform whether to use line wise or character wise selection
     * when the mouse is used.
     * @param lineWise
     */
    void setLineWiseMouseSelection(boolean lineWise);

    /**
     * Tells the platform that the editor should be closed.
     * @param force if set to true, closes unsaved documents.
     * @return whether the editor was closed or not.
     */
    boolean close(boolean force);

    /**
     * Formats the entire document according to the user's settings.
     */
    void format(Selection s);

    /**
     * Sets a named mark at the current position.
     * 
     * @param name the name of the mark.
     */
    void setMark(String name);

    /**
     * Returns the specified mark.
     * 
     * @param name
     *            the name of the mark.
     * @return the mark with the given name.
     */
    Mark getMark(String name);
}