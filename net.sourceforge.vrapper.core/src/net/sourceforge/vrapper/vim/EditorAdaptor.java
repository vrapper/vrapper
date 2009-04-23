package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public interface EditorAdaptor {
    void changeMode(String modeName);
    EditorMode getMode(String name);
    public boolean handleKey(KeyStroke key);

    TextContent getModelContent();
    TextContent getViewContent();
    CursorService getCursorService();
    FileService getFileService();
    ViewportService getViewportService();
    HistoryService getHistory();
    RegisterManager getRegisterManager();
    UserInterfaceService getUserInterfaceService();
    KeyMapProvider getKeyMapProvider();

    Position getPosition();
    void setPosition(Position destination, boolean updateStickyColumn);
    void setSelection(TextRange range);
    TextRange getSelection();
    <T>T getService(Class<T> serviceClass);
}

