package net.sourceforge.vrapper.vim;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public class DefaultEditorAdaptor implements EditorAdaptor {

    private EditorMode currentMode;
    private final Map<String, EditorMode> modeMap = new HashMap<String, EditorMode>();
    private final TextContent modelContent;
    private final TextContent viewContent;
    private final CursorService cursorService;
    private final SelectionService selectionService;
    private final FileService fileService;
    private final RegisterManager registerManager;
    private final ViewportService viewportService;
    private final HistoryService historyService;
    private final ServiceProvider serviceProvider;

    public DefaultEditorAdaptor(Platform editor, RegisterManager registerManager) {
        this.modelContent = editor.getModelContent();
        this.viewContent = editor.getViewContent();
        this.cursorService = editor.getCursorService();
        this.selectionService = editor.getSelectionService();
        this.historyService = editor.getHistoryService();
        this.registerManager = registerManager;
        this.serviceProvider = editor.getServiceProvider();
        viewportService = editor.getViewportService();

        fileService = editor.getFileService();
        EditorMode[] modes = {
                new NormalMode(this),
                new VisualMode(this),
                new InsertMode(this) };
        for (EditorMode mode: modes) {
            modeMap.put(mode.getName(), mode);
        }
        changeMode(NormalMode.NAME);
    }

    public void changeMode(String modeName) {
        EditorMode newMode = modeMap.get(modeName);
        if (newMode == null) {
            VrapperLog.error(format("There is no mode named '%s'",  modeName));
            return;
        }
        if (currentMode != newMode) {
            if (currentMode != null) {
                currentMode.leaveMode();
            }
            currentMode = newMode;
            newMode.enterMode();
        }
        // TODO: we may set "-- MODE NAME --" message here
    }

    public boolean handleKey(KeyStroke key) {
        if (currentMode != null) {
            return currentMode.handleKey(key);
        }
        return false;
    }

    public TextContent getModelContent() {
        return modelContent;
    }

    public TextContent getViewContent() {
        return viewContent;
    }

    public Position getPosition() {
        return cursorService.getPosition();
    }

    public void setPosition(Position destination, boolean updateStickyColumn) {
        cursorService.setPosition(destination, updateStickyColumn);
    }

    public TextRange getSelection() {
        return selectionService.getSelection();
    }

    public void setSelection(TextRange selection) {
        selectionService.setSelection(selection);
    }

    public CursorService getCursorService() {
        return cursorService;
    }

    public FileService getFileService() {
        return fileService;
    }

    public ViewportService getViewportService() {
        return viewportService;
    }

    public RegisterManager getRegisterManager() {
        return registerManager;
    }

    public HistoryService getHistory() {
        return historyService;
    }

    public <T> T getService(Class<T> serviceClass) {
        return serviceProvider.getService(serviceClass);
    }

}

