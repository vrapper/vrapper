package net.sourceforge.vrapper.vim;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.UnmodifiableTextContentDecorator;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.RecordMacroMode;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.YankOperation;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.BlockwiseVisualMode;
import net.sourceforge.vrapper.vim.modes.ConfirmSubstitutionMode;
import net.sourceforge.vrapper.vim.modes.ContentAssistMode;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertExpandMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.ReplaceMode;
import net.sourceforge.vrapper.vim.modes.TempLinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.TempNormalMode;
import net.sourceforge.vrapper.vim.modes.TempVisualMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;
import net.sourceforge.vrapper.vim.modes.commandline.HighlightSearch;
import net.sourceforge.vrapper.vim.modes.commandline.MessageMode;
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
    protected EditorMode currentMode;
    private final Map<String, EditorMode> modeMap = new HashMap<String, EditorMode>();
    private final TextContent modelContent;
    private final TextContent viewContent;
    private final CursorService cursorService;
    private final SelectionService selectionService;
    private final FileService fileService;
    private final BufferAndTabService bufferAndTabService;
    private RegisterManager registerManager;
    private final RegisterManager globalRegisterManager;
    private final ViewportService viewportService;
    private final HistoryService historyService;
    private final UserInterfaceService userInterfaceService;
    private final ServiceProvider serviceProvider;
    private final KeyStrokeTranslator keyStrokeTranslator;
    private final KeyMapProvider keyMapProvider;
    private final DefaultTextObjectProvider textObjectProvider;
    private final UnderlyingEditorSettings editorSettings;
    private final LocalConfiguration configuration;
    private final PlatformSpecificStateProvider platformSpecificStateProvider;
    private final PlatformSpecificModeProvider platformSpecificModeProvider;
    private final SearchAndReplaceService searchAndReplaceService;
    private final HighlightingService highlightingService;
    private MacroRecorder macroRecorder;
    private MacroPlayer macroPlayer;
    private String lastModeName;
    private String editorType;
    private VrapperEventListeners listeners;
    /** Previous selection, or null if editor is fresh. Must be editor-local. */
    private Selection lastSelection;
    private SearchResult searchResult;
    private int cursorBeforeMapping = -1;


    public DefaultEditorAdaptor(final Platform editor, final RegisterManager registerManager, final boolean isActive) {
        this.configuration = editor.getConfiguration();
        userInterfaceService = editor.getUserInterfaceService();
        this.modelContent = new UnmodifiableTextContentDecorator(editor.getModelContent(),
                                    configuration, editor);
        this.viewContent = new UnmodifiableTextContentDecorator(editor.getViewContent(),
                                    configuration, editor);
        this.cursorService = editor.getCursorService();
        this.selectionService = editor.getSelectionService();
        this.historyService = editor.getHistoryService();
        this.registerManager = registerManager;
        this.globalRegisterManager = registerManager;
        this.serviceProvider = editor.getServiceProvider();
        this.editorSettings = editor.getUnderlyingEditorSettings();
        textObjectProvider = new DefaultTextObjectProvider();
        PlatformSpecificTextObjectProvider specificTextObjectProvider =
                editor.getPlatformSpecificTextObjectProvider();
        if (specificTextObjectProvider != null) {
            textObjectProvider.updateDelimitedTexts(specificTextObjectProvider.delimitedTexts());
            textObjectProvider.updateTextObjects(specificTextObjectProvider.textObjects());
        }
        this.platformSpecificStateProvider = editor.getPlatformSpecificStateProvider(textObjectProvider);
        this.platformSpecificModeProvider = editor.getPlatformSpecificModeProvider();
        this.searchAndReplaceService = editor.getSearchAndReplaceService();
        viewportService = editor.getViewportService();
        this.highlightingService = editor.getHighlightingService();
        keyMapProvider = editor.getKeyMapProvider();
        keyStrokeTranslator = new KeyStrokeTranslator();
        macroRecorder = new MacroRecorder(registerManager, userInterfaceService);
        macroPlayer = null;
        this.editorType = editor.getEditorType();
        listeners = new VrapperEventListeners(this);
        fileService = editor.getFileService();
        bufferAndTabService = editor.getBufferAndTabService();

        __set_modes(this);
        setNewLineFromFirstLine();
        if (isActive) {
            changeModeSafely(NormalMode.NAME);
        }
        // NOTE: Read the config _after_ changing mode to allow default keys
        //       remapping.
        readConfiguration();

        // Set 'modifiable' flag if not done so by an autocmnd in .vrapperc
        if ("matchreadonly".equals(configuration.get(Options.SYNC_MODIFIABLE))
                && ! configuration.isSet(Options.MODIFIABLE)) {
            configuration.set(Options.MODIFIABLE, ( ! fileService.isReadOnly()));
        }
        // Check if this fresh editor needs immediate highlighting.
        String highlightScope = configuration.get(Options.SEARCH_HL_SCOPE);
        if (configuration.get(Options.SEARCH_HIGHLIGHT)
                && HighlightSearch.SEARCH_HL_SCOPE_WINDOW.equals(highlightScope)) {
            try {
                Search lastSearch = registerManager.getSearch();
                if (lastSearch != null) {
                    searchAndReplaceService.highlight(lastSearch);
                }
            } catch (VrapperPlatformException e) {
                VrapperLog.error("Failed to initialize highlighting in editor", e);
            }
        }
    }

    public String getLastModeName() {
        return lastModeName;
    }

    // this is public just for test purposes (Mockito spy as self)
    public void __set_modes(final DefaultEditorAdaptor self) {
        modeMap.clear();
        final EditorMode[] modes = {
                new NormalMode(self),
                new RecordMacroMode(self),
                new TempNormalMode(self),
                new VisualMode(self),
                new TempVisualMode(self),
                new LinewiseVisualMode(self),
                new TempLinewiseVisualMode(self),
                new BlockwiseVisualMode(self),
                new InsertMode(self),
                new InsertExpandMode(self),
                new ReplaceMode(self),
                new CommandLineMode(self),
                new SearchMode(self),
                new ConfirmSubstitutionMode(self),
                new MessageMode(self),
                new ContentAssistMode(self),
                new PasteRegisterMode(self)};
        for (final EditorMode mode: modes) {
            modeMap.put(mode.getName(), mode);
        }
    }

    @Override
    public void changeModeSafely(final String name, final ModeSwitchHint... hints) {
        try {
            changeMode(name, hints);
        } catch (final CommandExecutionException e) {
            VrapperLog.error("exception when changing mode",  e);
            userInterfaceService.setErrorMessage(e.getMessage());
        } catch (RuntimeException e) {
            VrapperLog.error("unexpected exception when changing mode",  e);
            userInterfaceService.setErrorMessage("error while changing to mode " + name);
        }
    }

    private void setNewLineFromFirstLine() {
        if (modelContent.getNumberOfLines() > 1) {
            final LineInformation first = modelContent.getLineInformation(0);
            final LineInformation second = modelContent.getLineInformation(1);
            final int start = first.getEndOffset();
            final int end = second.getBeginOffset();
            final String newLine = modelContent.getText(start, end-start);
            configuration.setNewLine(newLine);
        }
    }

    private void readConfiguration() {
        if (!SHOULD_READ_RC_FILE) {
            return;
        }
        try {
            configuration.setListenersEnabled(false);
            String filename = CONFIG_FILE_NAME;
            final File homeDir = new File(System.getProperty("user.home"));
            File config = new File(homeDir, filename);
            if( ! config.exists()) { //if no .vrapperrc, look for _vrapperrc
                filename = WINDOWS_CONFIG_FILE_NAME;
                config =  new File(homeDir, filename);
            }

            if (config.exists()) {
                sourceConfigurationFile(filename);
            }
        } finally {
            configuration.setListenersEnabled(true);
        }
    }

    @Override
    public boolean sourceConfigurationFile(final String filename) {
        File config = new File(filename);
        if( ! config.isAbsolute()) {
            final File homeDir = new File(System.getProperty("user.home"));
            // Ignore '~' character at the front - we resolve relative to home anyway.
            if (filename.startsWith("~/") || filename.startsWith("~\\")) {
                config = new File(homeDir, filename.substring(2));
            } else {
                config = new File(homeDir, filename);
            }
        }

        int lineNr = 0;
        if(config.exists()) {
        	BufferedReader reader = null;
        	try {
        		reader = new BufferedReader(new InputStreamReader(
        					new FileInputStream(config), "UTF-8"));
        		String line;
        		CommandLineMode cmdLineMode = (CommandLineMode) modeMap.get(CommandLineMode.NAME);
        		final CommandLineParser parser = cmdLineMode.createParser();
        		String trimmed;
        		while((line = reader.readLine()) != null) {
        		    lineNr++;
        			//*** skip over everything in a .vimrc file that we don't support ***//
        			trimmed = line.trim().toLowerCase();
        			//ignore comments and key mappings we don't support
        			if(trimmed.equals("") || trimmed.startsWith("\"") 
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
        			if(trimmed.startsWith(":")) {
        			    //leading ':' is optional, skip it if it exists
        			    line = line.substring(line.indexOf(':') +1);
        			}
        			//attempt to parse this line
        			Command c = parser.parseAndExecute(null, line.trim());
        			if (c != null) {
        			    c.execute(this);
        			}
        			
        		}
        	} catch (final IOException e) {
        		VrapperLog.error("Failed to parse .vrapperrc", e);
        	} catch (CommandExecutionException e) {
        		VrapperLog.error("Failed to execute command on line " + lineNr
        				+ " of .vrapperrc", e);
            } finally {
        		if(reader != null) {
        			try {
        				reader.close();
        			} catch (final IOException e) {
        				VrapperLog.error("Failed to close reader to .vrapperrc");
        			}
        		}
        	}
        	return true;
        }
        else {
        	return false;
        }
    }


    @Override
    public void changeMode(final String modeName, final ModeSwitchHint... args) throws CommandExecutionException {
        EditorMode newMode = modeMap.get(modeName);
        if (newMode == null) {
            // Load extension modes
            final List<EditorMode> modes = platformSpecificModeProvider.getModes(this);
            for (final EditorMode mode : modes) {
                if (modeMap.containsKey(mode.getName())) {
                    VrapperLog.error(format("Mode '%s' was already loaded!", mode.getName()));
                } else {
                    modeMap.put(mode.getName(), mode);
                }
            }
            newMode = modeMap.get(modeName);
            if (newMode == null) {
                VrapperLog.error(format("There is no mode named '%s'",  modeName));
                return;
            }
        }
        if (currentMode != newMode) {
            listeners.fireModeAboutToSwitch(newMode);
        	final EditorMode oldMode = currentMode;
            if (currentMode != null) {
                currentMode.leaveMode(args);
                lastModeName = currentMode.getName();
            }
            try {
            	currentMode = newMode;
            	newMode.enterMode(args);
            	//EditorMode might have called changeMode again, so update UI with actual mode.
            	userInterfaceService.setEditorMode(currentMode.getDisplayName());
            	listeners.fireModeSwitched(oldMode);
            }
            catch(final CommandExecutionException e) {
            	//failed to enter new mode, revert to previous mode
            	//then let Exception bubble up
            	currentMode = oldMode;
            	oldMode.enterMode();
            	//EditorMode might have called changeMode again, so update UI with actual mode.
            	userInterfaceService.setEditorMode(currentMode.getDisplayName());
            	listeners.fireModeSwitched(oldMode);
            	throw e;
            }
            catch (RuntimeException e) {
                //failed to enter new mode, revert to previous mode
                //then let Exception bubble up
                currentMode = oldMode;
                oldMode.enterMode();
                //EditorMode might have called changeMode again, so update UI with actual mode.
                userInterfaceService.setEditorMode(currentMode.getDisplayName());
                listeners.fireModeSwitched(oldMode);
                throw e;
            }
        }
    }

    @Override
    public boolean handleKey(final KeyStroke key) {
        macroRecorder.handleKey(key);
        return handleKeyOffRecord(key);
    }

    @Override
    public boolean handleKeyOffRecord(final KeyStroke key) {
        final boolean result = handleKey0(key);
        if (macroPlayer != null) {
            // while playing back one macro, another macro might be called
            // recursively. we need a fresh macro player for that.
            final MacroPlayer player = macroPlayer;
            macroPlayer = null;
            player.play();
        }
        return result;
    }

    @Override
    public TextContent getModelContent() {
        return modelContent;
    }

    @Override
    public TextContent getViewContent() {
        return viewContent;
    }

    @Override
    public Position getPosition() {
        return cursorService.getPosition();
    }

    @Override
    public void setPosition(final Position destination, final StickyColumnPolicy stickyColumnPolicy) {
        cursorService.setPosition(destination, stickyColumnPolicy);
    }

    @Override
    public Selection getSelection() {
        return selectionService.getSelection();
    }

    @Override
    public void setSelection(final Selection selection) {
        Set<String> cbOption = configuration.get(Options.CLIPBOARD);
        if (selection != null && selection.getModelLength() > 0
                && currentMode instanceof AbstractVisualMode) {
            Register activeRegister = registerManager.getActiveRegister();
            TextRange textRange;
            ContentType contentType;
            try {
                textRange = selection.getRegion(this, 0);
                contentType = selection.getContentType(configuration);
            } catch (CommandExecutionException e) {
                // Log and do nothing - this shouldn't happen, but one never knows.
                VrapperLog.error("Failed to get properties of selection!", e);
                return;
            }
            if (cbOption.contains(RegisterManager.CLIPBOARD_VALUE_AUTOSELECT)) {
                registerManager.setActiveRegister(RegisterManager.REGISTER_NAME_SELECTION);
                YankOperation.doIt(this, textRange, contentType, false, false);
            }
            if (cbOption.contains(RegisterManager.CLIPBOARD_VALUE_AUTOSELECTPLUS)) {
                registerManager.setActiveRegister(RegisterManager.REGISTER_NAME_CLIPBOARD);
                YankOperation.doIt(this, textRange, contentType, false, false);
            }
            registerManager.setActiveRegister(activeRegister);
        }
        
        selectionService.setSelection(selection);
    }

    @Override
    public void setNativeSelection(TextRange range) {
        selectionService.setNativeSelection(range);
    }

    @Override
    public TextRange getNativeSelection() {
        return selectionService.getNativeSelection();
    }

    @Override
    public BufferAndTabService getBufferAndTabService() {
        return bufferAndTabService;
    }

    @Override
    public CursorService getCursorService() {
        return cursorService;
    }

    @Override
    public FileService getFileService() {
        return fileService;
    }

    @Override
    public ViewportService getViewportService() {
        return viewportService;
    }

    @Override
    public UserInterfaceService getUserInterfaceService() {
        return userInterfaceService;
    }

    @Override
    public RegisterManager getRegisterManager() {
        return registerManager;
    }

    @Override
    public HistoryService getHistory() {
        return historyService;
    }

    @Override
    public <T> T getService(final Class<T> serviceClass) {
        return serviceProvider.getService(serviceClass);
    }

    @Override
    public EditorMode getMode(final String name) {
        return modeMap.get(name);
    }

    @Override
    public KeyMapProvider getKeyMapProvider() {
        return keyMapProvider;
    }
    
    @Override
    public TextObjectProvider getTextObjectProvider() {
        return textObjectProvider;
    }

    @Override
    public UnderlyingEditorSettings getEditorSettings() {
        return editorSettings;
    }

    @Override
    public PlatformSpecificStateProvider getPlatformSpecificStateProvider() {
        return platformSpecificStateProvider;
    }

    @Override
    public SearchAndReplaceService getSearchAndReplaceService() {
        return searchAndReplaceService;
    }

    @Override
    public HighlightingService getHighlightingService() {
        return highlightingService;
    }

    @Override
    public void useGlobalRegisters() {
        registerManager = globalRegisterManager;
        swapMacroRecorder();
    }

    @Override
    public void useLocalRegisters() {
        registerManager = new DefaultRegisterManager();
        swapMacroRecorder();
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public MacroRecorder getMacroRecorder() {
        return macroRecorder;
    }

    @Override
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
            KeyMap map = null;
            String keyMapName = currentMode.resolveKeyMap(key);
            if (keyMapName != null) {
                map = keyMapProvider.getKeyMap(keyMapName);
            }
            if (map != null) {
                final boolean inMapping = keyStrokeTranslator.processKeyStroke(map, key);
                if (inMapping) {
                    final Queue<RemappedKeyStroke> resultingKeyStrokes =
                        keyStrokeTranslator.resultingKeyStrokes();
                    //if we're in a mapping in InsertMode, display the pending characters
                    //(we'll delete them if the user completes the mapping)
                    if(currentMode.getName() == InsertMode.NAME) {
                        if(cursorBeforeMapping == -1) {
                            //keep track of where the cursor is at the start of a mapping
                            //so we can delete the pending characters if the mapping completes
                            cursorBeforeMapping = cursorService.getPosition().getModelOffset();
                        }
                    	//display pending character
                    	if(resultingKeyStrokes.isEmpty()) {
                    		return currentMode.handleKey(key);
                    	}
                    	//there are resulting key strokes,
                    	//mapping exited either successfully or unsuccessfully.
                    	//do we have pending characters to delete?
                    	else if(cursorService.getPosition().getModelOffset() - cursorBeforeMapping > 0) {
                    	    int pendingChars = cursorService.getPosition().getModelOffset() - cursorBeforeMapping;
                    	    //prepare for next mapping
                    	    cursorBeforeMapping = -1;
                    		if(keyStrokeTranslator.didMappingSucceed()) {
                    			//delete all the pending characters we had displayed
                    			for(int i=0; i < pendingChars; i++) {
                    				currentMode.handleKey(new RemappedKeyStroke(new SimpleKeyStroke(SpecialKey.BACKSPACE), false));
                    			}
                    		}
                    		else {
                    			// fixing the annoying jj-<Esc> bug.
                    			if (keyStrokeTranslator.needFixjjEsc()) {
                    				cursorBeforeMapping = cursorService.getPosition().getModelOffset();
                    			}
                    			//we've already displayed all but this most recent key

                    			//mapping failed though, check if key is in global map.
                    			if (KeyMap.GLOBAL_MAP.containsKey(key)) {
                    				key = new RemappedKeyStroke(KeyMap.GLOBAL_MAP.get(key), false);
                    			}
                    			return currentMode.handleKey(key);
                    		}
                    	}
                    	//else, mapping is only one character long (no pending characters to remove)
                    	
                    	//prepare for next mapping
                    	cursorBeforeMapping = -1;
                    } else if (resultingKeyStrokes.isEmpty()) {
                        currentMode.addKeyToMapBuffer(key);
                    } else {
                        currentMode.cleanMapBuffer(keyStrokeTranslator.didMappingSucceed());
                    }
                    //play all resulting key strokes
                    while (!resultingKeyStrokes.isEmpty()) {
                        final RemappedKeyStroke next = resultingKeyStrokes.poll();
                        if (next.isRecursive()) {
                            handleKey(next);
                        } else {
                            currentMode.handleKey(next);
                        }
                    }
                    return true;
                }
            }
            if (KeyMap.GLOBAL_MAP.containsKey(key)) {
                key = new RemappedKeyStroke(KeyMap.GLOBAL_MAP.get(key), false);
            }
            return currentMode.handleKey(key);
        }
        return false;
    }

    @Override
    public void onChangeEnabled(final boolean enabled) {
        // switch mode for set-up/tear-down
        if (enabled) {
            // Check if selection is present, if so initialize to visual mode.
            Selection eclipseSelection = getSelection();
            if (eclipseSelection.getModelLength() > 0) {
                rememberLastActiveSelection();
                changeModeSafely(VisualMode.NAME, VisualMode.RECALL_SELECTION_HINT);
            } else {
                changeModeSafely(NormalMode.NAME, InsertMode.DONT_MOVE_CURSOR);
            }
        } else {
            changeModeSafely(InsertMode.NAME, InsertMode.DONT_MOVE_CURSOR, InsertMode.DONT_LOCK_HISTORY);
            userInterfaceService.setEditorMode(UserInterfaceService.VRAPPER_DISABLED);
        }
        listeners.fireVrapperToggled(enabled);
    }

    @Override
    public void rememberLastActiveSelection() {
        Selection selection = selectionService.getSelection();
        // Eclipse might change document contents through an Eclipse action so re-check the bounds.
        if (selection.getRightBound().getModelOffset() > getModelContent().getTextLength()) {
            VrapperLog.info("Selection was outside of bounds of document. Fixing...");
            selection = selection.reset(this, selection.getFrom(), selection.getTo());
        }
        registerManager.setLastActiveSelection(SelectionArea.getInstance(this, selection));
        cursorService.setMark(CursorService.LAST_SELECTION_START_MARK, selection.getStartMark(this));
        cursorService.setMark(CursorService.LAST_SELECTION_END_MARK, selection.getEndMark(this));
        cursorService.setMark(CursorService.INTERNAL_LAST_SELECT_FROM_MARK, selection.getFrom());
        cursorService.setMark(CursorService.INTERNAL_LAST_SELECT_TO_MARK, selection.getTo());
        lastSelection = selection;
    }

    @Override
    public SelectionArea getLastActiveSelectionArea() {
        return registerManager.getLastActiveSelectionArea();
    }

    @Override
    public Selection getLastActiveSelection() {
        return lastSelection;
    }

    @Override
    public SearchResult getLastSearchResult() {
        return searchResult;
    }

    @Override
    public void setLastSearchResult(SearchResult result) {
        this.searchResult = result;
    }

	@Override
    public String getCurrentModeName() {
		return currentMode != null ? currentMode.getName() : null;
	}

	@Override
    public EditorMode getCurrentMode() {
		return currentMode;
	}

    public String getEditorType() {
        return editorType;
    }
    
    @Override
    public void addVrapperEventListener(VrapperEventListener listener) {
        listeners.addEventListener(listener);
    }

    @Override
    public void removeVrapperEventListener(VrapperEventListener listener) {
        listeners.removeEventListener(listener);
    }

    @Override
    public VrapperEventListeners getListeners() {
        return listeners;
    }

    @Override
    public CommandLineUI getCommandLine() {
        return userInterfaceService.getCommandLineUI(this);
    }

}
