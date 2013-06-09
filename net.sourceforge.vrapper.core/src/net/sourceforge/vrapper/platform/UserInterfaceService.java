package net.sourceforge.vrapper.platform;

/**
 * Provides access to vim-like mechanisms for showing information about the
 * editor state: command line, active mode, info and error message.<br>
 * Whether and how this information is displayed is entirely up to the
 * implementation.
 *
 * @author Matthias Radig
 */
public interface UserInterfaceService {
	
    static final String VRAPPER_DISABLED = "vrapper disabled";

    /**
     * The command line is used for showing the currently typed command or search.
     */
    void setCommandLine(String content, int position);

    /**
     * Indicates the current mode of the editor.
     */
    void setEditorMode(String modeName);
    
    /**
     * Get editor mode value
     */
    String getCurrentEditorMode();
    
    /**
     * For the :ascii command - prints ASCII values of the char under the cursor
     */
    void setAsciiValues(String asciiValue, int decValue, String hexValue, String octalValue);
    
    /**
     * 
     */
    String getLastCommandResultValue();
    
    /**
     * 
     */
    void setLastCommandResultValue(String lastCommandResultValue);
    
    /**
     * Message of any kind.
     */
    void setInfoMessage(String content);
    
    /**
     * Get last Info status message
     */
    String getLastInfoValue();

    /**
     * Error message, e.g. no search results found.
     */
    void setErrorMessage(String content);
    
    /**
     * Get last Error status message
     */
    String getLastErrorValue();

    /**
     * Whether a macro is currently being recorded.
     */
    void setRecording(boolean recording, String macroName);
   
    /**
     * Set to true when running an :ascii/ga command. 
     * Set to false when completing command. Keeps Info line from getting wiped out.
     * @param asciiSet
     */
    void setInfoSet(boolean infoSet);
   
    /**
     * Called in CommandBasedMode to determine whether or not to wipe out the Info line text.
     */
    boolean isInfoSet();
    
    /**
     * Editor split direction.
     */
    enum SplitDirection
    {
        VERTICALLY,
        HORIZONTALLY
    }
    enum SplitMode
    {
        CLONE,  // Make a clone of the current editor (vim behavior).
        MOVE,   // Move current editor into the new split.
    }
    /**
     * Split editor in the specified directions.
     */
    void splitEditor(SplitDirection dir, SplitMode mode);

    /**
     * Position relative to the current editor.
     */
    enum Where {
        UP,
        DOWN,
        LEFT,
        RIGHT,
    }

    /**
     * Activate editor positioned relatively to the current editor.
     */
    void switchEditor(Where where);

    /**
     * Move the current editor into the specified adjacent split.
     */
    void moveEditor(Where where, SplitMode mode);
}
