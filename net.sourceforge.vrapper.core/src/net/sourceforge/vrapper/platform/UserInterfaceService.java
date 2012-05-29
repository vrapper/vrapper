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
     * Message of any kind.
     */
    void setInfoMessage(String content);

    /**
     * Error message, e.g. no search results found.
     */
    void setErrorMessage(String content);

    /**
     * Whether a macro is currently being recorded.
     */
    void setRecording(boolean recording);
}
