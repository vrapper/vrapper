package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public interface EditorAdaptor {
    void changeMode(String modeName, ModeSwitchHint... args) throws CommandExecutionException;
    void changeModeSafely(String name, ModeSwitchHint... args);
    String getCurrentModeName();
    void onChangeEnabled(boolean enabled);
    void beginMouseSelection();
    EditorMode getMode(String name);
    public boolean handleKey(KeyStroke key);

    /**
     * Handles a key without, but does not pass the key to the macro recorder.
     * To be used when executing a macro.
     * @param key the key stroke to be handled
     * @return whether the key could be handled
     */
    public boolean handleKeyOffRecord(KeyStroke key);

    TextContent getModelContent();
    TextContent getViewContent();
    CursorService getCursorService();
    FileService getFileService();
    ViewportService getViewportService();
    HistoryService getHistory();
    RegisterManager getRegisterManager();
    UserInterfaceService getUserInterfaceService();
    KeyMapProvider getKeyMapProvider();
    UnderlyingEditorSettings getEditorSettings();
    Configuration getConfiguration();
    MacroRecorder getMacroRecorder();
    MacroPlayer getMacroPlayer();
    PlatformSpecificStateProvider getPlatformSpecificStateProvider();
    SearchAndReplaceService getSearchAndReplaceService();

    Position getPosition();
    void setPosition(Position destination, boolean updateStickyColumn);
    void setSelection(Selection selection);
    Selection getSelection();
    <T>T getService(Class<T> serviceClass);
    void useGlobalRegisters();
    void useLocalRegisters();
	void rememberLastActiveSelection();
	TextObject getLastActiveSelection();
}

