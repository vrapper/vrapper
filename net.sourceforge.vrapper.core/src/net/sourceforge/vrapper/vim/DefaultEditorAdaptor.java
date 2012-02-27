package net.sourceforge.vrapper.vim;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.PositionlessSelection;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.ReplaceMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public class DefaultEditorAdaptor implements EditorAdaptor {

    // ugly global option, so unit tests can disable it
    // in order to be .vrapperrc-agnostic
    public static boolean SHOULD_READ_RC_FILE = true;

    private static final String CONFIG_FILE_NAME = ".vrapperrc";
    private EditorMode currentMode;
    private final Map<String, EditorMode> modeMap = new HashMap<String, EditorMode>();
    private final TextContent modelContent;
    private final TextContent viewContent;
    private final CursorService cursorService;
    private final SelectionService selectionService;
    private final FileService fileService;
    private RegisterManager registerManager;
    private final RegisterManager globalRegisterManager;
    private final ViewportService viewportService;
    private final HistoryService historyService;
    private final UserInterfaceService userInterfaceService;
    private final ServiceProvider serviceProvider;
    private final KeyStrokeTranslator keyStrokeTranslator;
    private final KeyMapProvider keyMapProvider;
    private final UnderlyingEditorSettings editorSettings;
    private final LocalConfiguration configuration;
    private final PlatformSpecificStateProvider platformSpecificStateProvider;
    private final SearchAndReplaceService searchAndReplaceService;
    private MacroRecorder macroRecorder;
    private MacroPlayer macroPlayer;

    public DefaultEditorAdaptor(Platform editor, RegisterManager registerManager, boolean isActive) {
        this.modelContent = editor.getModelContent();
        this.viewContent = editor.getViewContent();
        this.cursorService = editor.getCursorService();
        this.selectionService = editor.getSelectionService();
        this.historyService = editor.getHistoryService();
        this.registerManager = registerManager;
        this.globalRegisterManager = registerManager;
        this.serviceProvider = editor.getServiceProvider();
        this.editorSettings = editor.getUnderlyingEditorSettings();
        this.configuration = new SimpleLocalConfiguration(editor.getConfiguration());
        this.platformSpecificStateProvider = editor.getPlatformSpecificStateProvider();
        this.searchAndReplaceService = editor.getSearchAndReplaceService();
        viewportService = editor.getViewportService();
        userInterfaceService = editor.getUserInterfaceService();
        keyMapProvider = editor.getKeyMapProvider();
        keyStrokeTranslator = new KeyStrokeTranslator();
        macroRecorder = new MacroRecorder(registerManager, userInterfaceService);
        macroPlayer = null;

        fileService = editor.getFileService();
        __set_modes(this);
        readConfiguration();
        setNewLineFromFirstLine();
        if (isActive) {
            changeModeSafely(NormalMode.NAME);
        }
    }

    // this is public just for test purposes (Mockito spy as self)
    public void __set_modes(DefaultEditorAdaptor self) {
        modeMap.clear();
        EditorMode[] modes = {
                new NormalMode(self),
                new VisualMode(self),
                new LinewiseVisualMode(self),
                new InsertMode(self),
                new ReplaceMode(self),
                new CommandLineMode(self),
                new SearchMode(self)};
        for (EditorMode mode: modes) {
            modeMap.put(mode.getName(), mode);
        }
    }

    public void changeModeSafely(String name, ModeSwitchHint... hints) {
        try {
            changeMode(name, hints);
        } catch (CommandExecutionException e) {
            VrapperLog.error("exception when changing mode",  e);
            userInterfaceService.setErrorMessage(e.getMessage());
        }
    }

    private void setNewLineFromFirstLine() {
        if (modelContent.getNumberOfLines() > 1) {
            LineInformation first = modelContent.getLineInformation(0);
            LineInformation second = modelContent.getLineInformation(1);
            int start = first.getEndOffset();
            int end = second.getBeginOffset();
            String newLine = modelContent.getText(start, end-start);
            configuration.setNewLine(newLine);
        }
    }

    private void readConfiguration() {
        if (!SHOULD_READ_RC_FILE) {
            return;
        }
        File homeDir = new File(System.getProperty("user.home"));
        File config = new File(homeDir, CONFIG_FILE_NAME);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(config));
            String line;
            CommandLineParser parser = new CommandLineParser(this);
            while((line = reader.readLine()) != null) {
                parser.parseAndExecute(null, line.trim());
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void changeMode(String modeName, ModeSwitchHint... args) throws CommandExecutionException {
        EditorMode newMode = modeMap.get(modeName);
        if (newMode == null) {
            VrapperLog.error(format("There is no mode named '%s'",  modeName));
            return;
        }
        if (currentMode != newMode) {
        	EditorMode oldMode = currentMode;
            if (currentMode != null) {
                currentMode.leaveMode(args);
            }
            try {
            	currentMode = newMode;
            	newMode.enterMode(args);
            	userInterfaceService.setEditorMode(newMode.getName());
            }
            catch(CommandExecutionException e) {
            	//failed to enter new mode, revert to previous mode
            	//then let Exception bubble up
            	currentMode = oldMode;
            	oldMode.enterMode();
            	throw e;
            }
        }
    }

    public boolean handleKey(KeyStroke key) {
        macroRecorder.handleKey(key);
        return handleKeyOffRecord(key);
    }

    public boolean handleKeyOffRecord(KeyStroke key) {
        boolean result = handleKey0(key);
        if (macroPlayer != null) {
            // while playing back one macro, another macro might be called
            // recursively. we need a fresh macro player for that.
            MacroPlayer player = macroPlayer;
            macroPlayer = null;
            player.play();
        }
        return result;
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

    public Selection getSelection() {
        return selectionService.getSelection();
    }

    public void setSelection(Selection selection) {
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

    public UserInterfaceService getUserInterfaceService() {
        return userInterfaceService;
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

    public EditorMode getMode(String name) {
        return modeMap.get(name);
    }

    public KeyMapProvider getKeyMapProvider() {
        return keyMapProvider;
    }

    public UnderlyingEditorSettings getEditorSettings() {
        return editorSettings;
    }

    public PlatformSpecificStateProvider getPlatformSpecificStateProvider() {
        return platformSpecificStateProvider;
    }

    public SearchAndReplaceService getSearchAndReplaceService() {
        return searchAndReplaceService;
    }

    public void useGlobalRegisters() {
        registerManager = globalRegisterManager;
        swapMacroRecorder();
    }

    public void useLocalRegisters() {
        registerManager = new DefaultRegisterManager();
        swapMacroRecorder();
    }

    public LocalConfiguration getConfiguration() {
        return configuration;
    }

    public MacroRecorder getMacroRecorder() {
        return macroRecorder;
    }

    public MacroPlayer getMacroPlayer() {
        if (macroPlayer == null) {
            macroPlayer = new MacroPlayer(this);
        }
        return macroPlayer;
    }

    private void swapMacroRecorder() {
        if (macroRecorder.isRecording()) {
            macroRecorder.stopRecording();
        }
        macroRecorder = new MacroRecorder(registerManager, userInterfaceService);
    }

    private boolean handleKey0(KeyStroke key) {
        if (currentMode != null) {
            KeyMap map = currentMode.resolveKeyMap(keyMapProvider);
            if (map != null) {
                boolean inMapping = keyStrokeTranslator.processKeyStroke(map, key);
                if (inMapping) {
                    Queue<RemappedKeyStroke> resultingKeyStrokes =
                        keyStrokeTranslator.resultingKeyStrokes();
                    while (!resultingKeyStrokes.isEmpty()) {
                        RemappedKeyStroke next = resultingKeyStrokes.poll();
                        if (next.isRecursive()) {
                            handleKey(next);
                        } else {
                            currentMode.handleKey(next);
                        }
                    }
                    return true;
                }
            }
            return currentMode.handleKey(key);
        }
        return false;
    }

    public void onChangeEnabled(boolean enabled) {
        // switch mode for set-up/tear-down
        changeModeSafely(enabled ? NormalMode.NAME : InsertMode.NAME,
                InsertMode.DONT_MOVE_CURSOR);
    }
    
	public void rememberLastActiveSelection() {
		registerManager.setLastActiveSelection(PositionlessSelection.getInstance(this));
	}

	public TextObject getLastActiveSelection() {
		return registerManager.getLastActiveSelection();
	}

	public String getCurrentModeName() {
		return currentMode != null ? currentMode.getName() : null;
	}

}
