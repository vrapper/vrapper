package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.register.RegisterManager;

/**
 * This facade gives access to all Vrapper functionality of the platform (i.e. the editor and IDE
 * infrastructure) while holding Vrapper's general state for the current editor.
 */
public interface EditorAdaptor {
    /** Called when the editor is about to close. */
    void close() throws CommandExecutionException;

    void changeMode(String modeName, ModeSwitchHint... args) throws CommandExecutionException;
    void changeModeSafely(String name, ModeSwitchHint... args);
    String getCurrentModeName();
    String getLastModeName();
    void onChangeEnabled(boolean enabled);
    EditorMode getMode(String name);
    EditorMode getCurrentMode();
    public boolean handleKey(KeyStroke key);
    String getEditorType();

    /**
     * Handles a key without, but does not pass the key to the macro recorder.
     * To be used when executing a macro.
     * @param key the key stroke to be handled
     * @return whether the key could be handled
     */
    public boolean handleKeyOffRecord(KeyStroke key);

    TextContent getModelContent();
    TextContent getViewContent();
    BufferAndTabService getBufferAndTabService();
    CursorService getCursorService();
    FileService getFileService();
    ViewportService getViewportService();
    HistoryService getHistory();
    RegisterManager getRegisterManager();
    UserInterfaceService getUserInterfaceService();
    KeyMapProvider getKeyMapProvider();
    TextObjectProvider getTextObjectProvider();
    UnderlyingEditorSettings getEditorSettings();
    LocalConfiguration getConfiguration();
    MacroRecorder getMacroRecorder();
    /** @throws CommandExecutionException when the requested macro could not be created
     *      due to deep nesting or certain recursive usage.
     */
    MacroPlayer getMacroPlayer(String macroName) throws CommandExecutionException;
    void stopMacrosAndMappings();
    void stopMacrosAndMappings(String errorMessage);
    PlatformSpecificStateProvider getPlatformSpecificStateProvider();
    SearchAndReplaceService getSearchAndReplaceService();
    HighlightingService getHighlightingService();

    boolean sourceConfigurationFile(String filename);
    /**
     * @return the current position in the text, i.e. where the caret is displayed. This position
     * will be corrected for inclusive selection mode if a selection is present.
     */
    Position getPosition();
    void setPosition(Position destination, StickyColumnPolicy stickyColumnPolicy);
    void setSelection(Selection selection);
    Selection getSelection();
    /**
     * Set the editor selection directly.
     * <p><b>NOTE</b>: Use sparingly, {@link #setSelection(Selection)} is still the expected method.
     *  @see SelectionService#setNativeSelection(TextRange)
     */
    void setNativeSelection(TextRange range);
    /**
     * Get information whether a selection is present. The length is only guaranteed to be either
     * <code>0</code> or <code>&gt; 0</code>.
     * <p><b>NOTE</b>: Use sparingly, {@link #getSelection()} is still the expected method.
     * @see SelectionService#getNativeSelection() SelectionService, for more info on return values.
     */
    TextRange getNativeSelection();
    <T>T getService(Class<T> serviceClass);
    void useGlobalRegisters();
    void useLocalRegisters();
	void rememberLastActiveSelection();
	TextObject getLastActiveSelectionArea();
    Selection getLastActiveSelection();
    /**
     * Return information about the last search match.
     * @return either an instance of SearchResult (possibly one for which `isFound` returns false!)
     *  or <code>null</code> if no search query has been executed yet.
     */
    SearchResult getLastSearchResult();
    void setLastSearchResult(SearchResult result);

    CommandLineUI getCommandLine();
    
    void addVrapperEventListener(VrapperEventListener listener);
    void removeVrapperEventListener(VrapperEventListener listener);
    VrapperEventListeners getListeners();
}

