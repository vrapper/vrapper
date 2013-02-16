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
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration.Option;
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
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.ReplaceMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;
import net.sourceforge.vrapper.vim.modes.commandline.PasteRegisterMode;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public class DefaultEditorAdaptor implements EditorAdaptor {

    // ugly global option, so unit tests can disable it
    // in order to be .vrapperrc-agnostic
    public static boolean SHOULD_READ_RC_FILE = true;

    private static final String CONFIG_FILE_NAME = ".vrapperrc";
    private static final String WINDOWS_CONFIG_FILE_NAME = "_vrapperrc";
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
        LocalConfigurationListener listener = new LocalConfigurationListener() {
            
            public <T> void optionChanged(Option<T> option, T oldValue, T newValue) {
                if("clipboard".equals(option.getId())) {
                    if("unnamed".equals(newValue)) {
                        Register clipboardRegister = DefaultEditorAdaptor.this.getRegisterManager().getRegister(RegisterManager.REGISTER_NAME_CLIPBOARD);
                        DefaultEditorAdaptor.this.getRegisterManager().setDefaultRegister(clipboardRegister);
                    } else {
                        Register unnamedRegister = DefaultEditorAdaptor.this.getRegisterManager().getRegister(RegisterManager.REGISTER_NAME_UNNAMED);
                        DefaultEditorAdaptor.this.getRegisterManager().setDefaultRegister(unnamedRegister);
                    }
                }
                
            }
        };
        this.configuration.addListener(listener);
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
                new SearchMode(self),
                new PasteRegisterMode(self)};
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
        String filename = CONFIG_FILE_NAME;
        File homeDir = new File(System.getProperty("user.home"));
        File config = new File(homeDir, filename);
        if( ! config.exists()) { //if no .vrapperrc, look for _vrapperrc
        	filename = WINDOWS_CONFIG_FILE_NAME;
        	config =  new File(homeDir, filename);
        }
        
        if(config.exists()) {
        	sourceConfigurationFile(filename);
        }
    }

    public void sourceConfigurationFile(String filename) {
        File homeDir = new File(System.getProperty("user.home"));
        File config = new File(homeDir, filename);
        
        if(config.exists()) {
        	BufferedReader reader = null;
        	try {
        		reader = new BufferedReader(new FileReader(config));
        		String line;
        		CommandLineParser parser = new CommandLineParser(this);
        		String trimmed;
        		while((line = reader.readLine()) != null) {
        			//*** skip over everything in a .vimrc file that we don't support ***//
        			trimmed = line.trim().toLowerCase();
        			//ignore comments and key mappings we don't support
        			if(trimmed.equals("") || trimmed.startsWith("\"") || trimmed.startsWith("let")
        					|| trimmed.contains("<leader>") || trimmed.contains("<silent>")) {
        				continue;
        			}
        			if(trimmed.startsWith("if")) {
        				//skip all conditional statements
        				while((line = reader.readLine()) != null) {
        					if(line.trim().toLowerCase().startsWith("endif")) {
        						break;
        					}
        				}
        				continue; //skip "endif" line
        			}
        			if(trimmed.startsWith("func")) {
        				//skip all function declarations
        				while((line = reader.readLine()) != null) {
        					if(line.trim().toLowerCase().startsWith("endfunc")) {
        						break;
        					}
        				}
        				continue; //skip "endfunction" line
        			}
        			if(trimmed.startsWith("try")) {
        				//skip all try declarations
        				while((line = reader.readLine()) != null) {
        					if(line.trim().toLowerCase().startsWith("endtry")) {
        						break;
        					}
        				}
        				continue; //skip "endtry" line
        			}
        			//attempt to parse this line
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
            	userInterfaceService.setEditorMode(newMode.getDisplayName());
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
        selectionService.setSelection(selection, false);
    }
    
    public void setSelection(Selection selection, boolean leaveVisualMode) {
        selectionService.setSelection(selection, leaveVisualMode);
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

    /**
     * Note from Kevin: This method is a major source of frustration for me.
     * It has to handle the following scenarios but I can't unit test them:
     * - multi-character mapping completed in InsertMode and NormalMode
     * - multi-character mapping *not* completed in InsertMode and NormalMode
     *   - "nmap zx gg" and type 'zt'
     *   - "imap jj <ESC>" and type 'jk'
     * - display pending character in InsertMode
     *   - and delete pending character when mapping completed
     * - don't move cursor when not completing a multi-character mapping while inside parentheses
     *   - "for(int j=0)"  when 'j' is part of a mapping ("imap jj <ESC>")
     *   - (Eclipse moves the cursor on me in its "Smart Insert" mode with auto-closing parentheses)
     * - single-character mapping in InsertMode (perform mapping but don't display pending character)
     */
    private boolean handleKey0(KeyStroke key) {
        if (currentMode != null) {
            KeyMap map = currentMode.resolveKeyMap(keyMapProvider);
            if (map != null) {
                boolean inMapping = keyStrokeTranslator.processKeyStroke(map, key);
                if (inMapping) {
                    Queue<RemappedKeyStroke> resultingKeyStrokes =
                        keyStrokeTranslator.resultingKeyStrokes();
                    //if we're in a mapping in InsertMode, display the pending characters
                    //(we'll delete them if the user completes the mapping)
                    if(currentMode.getName() == InsertMode.NAME) {
                    	//display pending character
                    	if(resultingKeyStrokes.isEmpty()) {
                    		return currentMode.handleKey(key);
                    	}
                    	//there are resulting key strokes,
                    	//mapping exited either successfully or unsuccessfully
                    	else if(keyStrokeTranslator.numUnconsumedKeys() > 0) {
                    		if(keyStrokeTranslator.didMappingSucceed()) {
                    			//delete all the pending characters we had displayed
                    			for(int i=0; i < keyStrokeTranslator.numUnconsumedKeys(); i++) {
                    				currentMode.handleKey(new RemappedKeyStroke(new SimpleKeyStroke(SpecialKey.BACKSPACE), false));
                    			}
                    		}
                    		else {
                    			//we've already displayed all but this most recent key
                    			return currentMode.handleKey(key);
                    		}
                    	}
                    	//else, mapping is only one character long (no pending characters to remove)
                    }
                    //play all resulting key strokes
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
        if (enabled) {
            // switch mode for set-up/tear-down
            changeModeSafely(NormalMode.NAME, InsertMode.DONT_MOVE_CURSOR);
        } else {
            changeModeSafely(InsertMode.NAME, InsertMode.DONT_MOVE_CURSOR, InsertMode.DONT_LOCK_HISTORY);
            userInterfaceService.setEditorMode(UserInterfaceService.VRAPPER_DISABLED);
        }
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
