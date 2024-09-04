
package net.sourceforge.vrapper.eclipse.interceptor;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.eclipse.platform.SWTRegisterManager;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.KeyStroke.Modifier;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.GlobalConfiguration;
import net.sourceforge.vrapper.platform.PlatformVrapperLifecycleListener;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.ConfigurationListener;
import net.sourceforge.vrapper.vim.DefaultConfigProvider;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.SimpleGlobalConfiguration;
import net.sourceforge.vrapper.vim.register.ReadOnlyRegister;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * A factory for interceptors which route input events to a {@link EditorAdaptor}
 * instance. This instance decides whether to pass the event to the underlying
 * editor or not.
 *
 * @author Matthias Radig
 */
public class VimInputInterceptorFactory implements InputInterceptorFactory {

    private VimInputInterceptorFactory() { /* NOP */ }

    public static final VimInputInterceptorFactory INSTANCE = new VimInputInterceptorFactory();

    private static final HashMap<Integer, SpecialKey> specialKeys;
    /** Maps "Escape characters" to the corresponding Control + <i>x</i> character. */
    private static final HashMap<Character, Character> escapedChars;
    private static final HashSet<Integer> ignoredKeyCodes;

    private static final GlobalConfiguration sharedConfiguration = setupGlobalConfiguration();

    private static final RegisterManager globalRegisterManager;

    static {
        specialKeys = createSpecialKeys();

        escapedChars = createEscapedChars();

        ignoredKeyCodes = createIgnoredKeyCodes();

        Map<String, Register> platformRegisters = new HashMap<String, Register>();

        platformRegisters.put(RegisterManager.REGISTER_NAME_CURRENT_FILE, new ReadOnlyRegister() {
            @Override
            public RegisterContent getContent() {
                InputInterceptor interceptor;
                try {
                    interceptor = VrapperPlugin.getDefault().findActiveInterceptor();
                    if (interceptor == null) {
                        return RegisterContent.DEFAULT_CONTENT;
                    }
                } catch (VrapperPlatformException e) {
                    VrapperLog.error("Failed to find active editor even though Vrapper was active", e);
                    return RegisterContent.DEFAULT_CONTENT;
                } catch (UnknownEditorException e) {
                    VrapperLog.error("Failed to find active editor even though Vrapper was active");
                    return RegisterContent.DEFAULT_CONTENT;
                }
                try {
                    return new StringRegisterContent(ContentType.TEXT,
                                interceptor.getPlatform().getFileService().getCurrentFileLocation());
                } catch (IOException e) {
                    VrapperLog.info("Current editor has no file associated with it");
                    return RegisterContent.DEFAULT_CONTENT;
                }
            }
        });
        globalRegisterManager = new SWTRegisterManager(PlatformUI.getWorkbench().getDisplay(),
                sharedConfiguration, platformRegisters);
    }

    /** Initialize default configuration where it needs to be overridden by environment. */
    private static GlobalConfiguration setupGlobalConfiguration() {
        DefaultConfigProvider globalDefaults = new DefaultConfigProvider() {
            @Override
            @SuppressWarnings("unchecked") // we need casting for our return values
            public <T> T getDefault(Option<T> option) {
                if (Options.SHOW_WHITESPACE.equals(option)) {
                    Boolean show = EditorsUI.getPreferenceStore().getBoolean(
                            AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_WHITESPACE_CHARACTERS);
                    return (T) show;
                } else if (Options.LINE_NUMBERS.equals(option)) {
                    Boolean show = EditorsUI.getPreferenceStore().getBoolean(
                            AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER);
                    return (T) show;
                } else if (Options.HIGHLIGHT_CURSOR_LINE.equals(option)) {
                    Boolean highlight = EditorsUI.getPreferenceStore().getBoolean(
                            AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE);
                    return (T) highlight;
                } else if (Options.SPELL.equals(option)) {
                    Boolean enable = EditorsUI.getPreferenceStore()
                            .getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED);
                    return (T) enable;
                } else {
                    return null;
                }
            }
        };
        ConfigurationListener configListener = new ConfigurationListener() {
            @Override
            public <T> void optionChanged(Option<T> option, T oldValue, T newValue) {
                if (Options.DEBUGLOG.equals(option)) {
                    VrapperLog.setDebugEnabled(Boolean.TRUE.equals(newValue));
                } else if (Options.SHOW_WHITESPACE.equals(option)) {
                    Boolean show = (Boolean) newValue;
                    EditorsUI.getPreferenceStore().setValue(
                            AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_WHITESPACE_CHARACTERS,
                            show);
                } else if (Options.LINE_NUMBERS.equals(option)) {
                    Boolean show = (Boolean) newValue;
                    EditorsUI.getPreferenceStore().setValue(
                            AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER,
                            show);
                } else if (Options.HIGHLIGHT_CURSOR_LINE.equals(option)) {
                    Boolean highlight = (Boolean) newValue;
                    EditorsUI.getPreferenceStore().setValue(
                            AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE,
                            highlight);
                } else if (Options.SPELL.equals(option)) {
                    Boolean enable = (Boolean) newValue;
                    EditorsUI.getPreferenceStore()
                            .setValue(SpellingService.PREFERENCE_SPELLING_ENABLED, enable);
                }
            }
        };
        List<DefaultConfigProvider> configProviders = Collections.singletonList(globalDefaults);
        GlobalConfiguration sharedConfiguration = new SimpleGlobalConfiguration(configProviders);
        // Sync debuglog option's value with actual Log setting (read from system properties).
        sharedConfiguration.set(Options.DEBUGLOG, VrapperLog.isDebugEnabled());
        sharedConfiguration.addListener(configListener);
        return sharedConfiguration;
    }

    private static HashMap<Integer, SpecialKey> createSpecialKeys() {
        HashMap<Integer, SpecialKey> specialKeys = new HashMap<Integer, SpecialKey>();
        specialKeys.put( SWT.ARROW_LEFT,         SpecialKey.ARROW_LEFT);
        specialKeys.put( SWT.ARROW_RIGHT,        SpecialKey.ARROW_RIGHT);
        specialKeys.put( SWT.ARROW_UP,           SpecialKey.ARROW_UP);
        specialKeys.put( SWT.ARROW_DOWN,         SpecialKey.ARROW_DOWN);
        specialKeys.put( (int)SWT.BS,            SpecialKey.BACKSPACE);
        specialKeys.put( (int)SWT.DEL,           SpecialKey.DELETE);
        specialKeys.put( (int)SWT.TAB,           SpecialKey.TAB);
        specialKeys.put( SWT.INSERT,             SpecialKey.INSERT);
        specialKeys.put( SWT.PAGE_DOWN,          SpecialKey.PAGE_DOWN);
        specialKeys.put( SWT.PAGE_UP,            SpecialKey.PAGE_UP);
        specialKeys.put( SWT.HOME,               SpecialKey.HOME);
        specialKeys.put( SWT.END,                SpecialKey.END);
        specialKeys.put( (int)SWT.ESC,           SpecialKey.ESC);
        specialKeys.put( (int)SWT.CR,            SpecialKey.RETURN);
        specialKeys.put( SWT.KEYPAD_CR,          SpecialKey.RETURN);

        SpecialKey[] values = SpecialKey.values();
        int swtStart = SWT.F1;
        int skStart = SpecialKey.F1.ordinal();
        //SWT has up to F20
        for (int i=0; i < 20; ++i)
            specialKeys.put(swtStart+i, values[skStart+i]);
        return specialKeys;
    }


    private static HashMap<Character, Character> createEscapedChars() {
        /*
         * Translate the "Escape characters" sent by Ctrl + alpha keys to regular characters.
         * This circumvents problems with the user language where raw keyCode values might not.
         */
        HashMap<Character, Character> escapedChars = new HashMap<Character, Character>();
        escapedChars.put('\u0000', '@');
        escapedChars.put('\u0001', 'a');
        escapedChars.put('\u0002', 'b');
        escapedChars.put('\u0003', 'c');
        escapedChars.put('\u0004', 'd');
        escapedChars.put('\u0005', 'e');
        escapedChars.put('\u0006', 'f');
        escapedChars.put('\u0007', 'g');
        escapedChars.put('\u0008', 'h');
        escapedChars.put('\t',     'i');
        escapedChars.put('\n',     'j');
        escapedChars.put('\u000B', 'k');
        escapedChars.put('\u000C', 'l');
        escapedChars.put('\r',     'm');
        escapedChars.put('\u000E', 'n');
        escapedChars.put('\u000F', 'o');
        escapedChars.put('\u0010', 'p');
        escapedChars.put('\u0011', 'q');
        escapedChars.put('\u0012', 'r');
        escapedChars.put('\u0013', 's');
        escapedChars.put('\u0014', 't');
        escapedChars.put('\u0015', 'u');
        escapedChars.put('\u0016', 'v');
        escapedChars.put('\u0017', 'w');
        escapedChars.put('\u0018', 'x');
        escapedChars.put('\u0019', 'y');
        escapedChars.put('\u001A', 'z');
        escapedChars.put('\u001B', '[');
        escapedChars.put('\u001C', '\\');
        escapedChars.put('\u001D', ']');
        escapedChars.put('\u001E', '^');
        escapedChars.put('\u001F', '_');
        return escapedChars;
    }

    private static HashSet<Integer> createIgnoredKeyCodes() {
        HashSet<Integer> ignoredKeyCodes = new HashSet<Integer>();
        ignoredKeyCodes.add(SWT.CTRL);
        ignoredKeyCodes.add(SWT.SHIFT);
        ignoredKeyCodes.add(SWT.ALT);
        ignoredKeyCodes.add(SWT.CAPS_LOCK);
        ignoredKeyCodes.add(SWT.COMMAND);
        return ignoredKeyCodes;
    }

    @Override
    public InputInterceptor createInterceptor(AbstractTextEditor abstractTextEditor,
            ISourceViewer textViewer, EditorInfo partInfo, BufferAndTabService bufferAndTabService,
            List<PlatformVrapperLifecycleListener> lifecycleListeners) {

        EclipsePlatform platform = new EclipsePlatform(partInfo, abstractTextEditor, textViewer,
                sharedConfiguration, bufferAndTabService);
        DefaultEditorAdaptor editorAdaptor = new DefaultEditorAdaptor(platform,
                globalRegisterManager, VrapperPlugin.isVrapperEnabled(), lifecycleListeners);
        editorAdaptor.addVrapperEventListener(platform.getModeRecorder());
        InputInterceptor interceptor = createInterceptor(editorAdaptor);
        interceptor.setPlatform(platform);
        interceptor.setEditorInfo(partInfo);
        platform.getServiceProvider().setInputInterceptor(interceptor);

        interceptor.setCaretPositionUndoHandler(new CaretPositionUndoHandler(editorAdaptor, textViewer));
        editorAdaptor.addVrapperEventListener(interceptor.getCaretPositionUndoHandler());

        interceptor.setCaretPositionHandler(new CaretPositionHandler(editorAdaptor, textViewer));

        if (editorAdaptor.getConfiguration().get(Options.EXIT_LINK_MODE)) {
            LinkedModeHandler linkedModeHandler = new LinkedModeHandler(editorAdaptor);
            LinkedModeHandler.registerListener(textViewer.getDocument(), linkedModeHandler);
            interceptor.setLinkedModeHandler(linkedModeHandler);
        }
        if (editorAdaptor.getConfiguration().get(Options.CONTENT_ASSIST_MODE)) {
            if (textViewer instanceof SourceViewer) {
                final SourceViewer sourceViewer = (SourceViewer)textViewer;
                final ContentAssistantFacade contentAssistant= sourceViewer.getContentAssistantFacade();
                if (contentAssistant != null) {
                    contentAssistant.addCompletionListener(new ContentAssistModeHandler(editorAdaptor));
                }
            }
        }

        interceptor.setEclipseCommandHandler(new EclipseCommandHandler(editorAdaptor));

        SelectionVisualHandler visualHandler = new SelectionVisualHandler(editorAdaptor,
                platform.getSelectionService(), textViewer, interceptor.getEclipseCommandHandler());
        interceptor.setSelectionVisualHandler(visualHandler);
        return interceptor;
    }

    public InputInterceptor createInterceptor(EditorAdaptor editorAdaptor) {
        return new VimInputInterceptor(editorAdaptor);
    }

    private static final class VimInputInterceptor implements InputInterceptor {

        private final EditorAdaptor editorAdaptor;
        private EditorInfo editorInfo;
        private LinkedModeHandler linkedModeHandler;
        private CaretPositionHandler caretPositionHandler;
        private SelectionVisualHandler selectionVisualHandler;
        private CaretPositionUndoHandler caretPositionUndoHandler;
        private EclipseCommandHandler eclipseCommandHandler;
        private EclipsePlatform eclipsePlatform;

        private VimInputInterceptor(EditorAdaptor editorAdaptor) {
            this.editorAdaptor = editorAdaptor;
        }

        public void verifyKey(VerifyEvent event) {
            if (!VrapperPlugin.isVrapperEnabled()) {
                return;
            }
            // Ignore event when modifier is held down before other key gets pressed
            if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
                return;
            }
            if (ignoredKeyCodes.contains(event.keyCode)) {
                return;
            }

            KeyStroke keyStroke;
            EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            if ((event.stateMask & SWT.SHIFT) != 0) {
                modifiers.add(Modifier.SHIFT);
            }
            if ((event.stateMask & SWT.ALT) != 0) {
                modifiers.add(Modifier.ALT);
            }
            if ((event.stateMask & SWT.COMMAND) != 0) {
                modifiers.add(Modifier.COMMAND);
            }
            boolean includesCtrlKey = (event.stateMask & SWT.CONTROL) != 0;
            if (includesCtrlKey) {
                modifiers.add(Modifier.CONTROL);
            }
            // No AltGr check here, we don't support it as a modifier

            // The keys where this bit is set should never be checked against the escapedChars set
            // because it's possible that event.character == 0, which would be confused with Ctrl+@
            boolean isPotentiallyEscapedKey = (event.keyCode & SWT.KEYCODE_BIT) == 0;

            if (specialKeys.containsKey(event.keyCode)) {
                keyStroke = new SimpleKeyStroke(specialKeys.get(event.keyCode), modifiers);
            } else if (isPotentiallyEscapedKey && includesCtrlKey
                    && escapedChars.containsKey(event.character)) {
                // Ctrl + A, B, ... gets received as 0x01, 0x02. Translate this to proper characters
                keyStroke = new SimpleKeyStroke(escapedChars.get(event.character), modifiers);
            } else {
                if (event.character == 0 && VrapperLog.isDebugEnabled()) {
                    VrapperLog.debug("Unrecognized key. Keycode: " + event.keyCode
                            + ", state: " + event.stateMask + ", modifiers: " + modifiers);
                }
                keyStroke = new SimpleKeyStroke(event.character, modifiers);
            }
            event.doit = !editorAdaptor.handleKey(keyStroke);
        }

        public EditorAdaptor getEditorAdaptor() {
            return editorAdaptor;
        }

        @Override
        public EditorInfo getEditorInfo() {
            return editorInfo;
        }

        @Override
        public void setEditorInfo(EditorInfo partInfo) {
            this.editorInfo = partInfo;
        }

        @Override
        public LinkedModeHandler getLinkedModeHandler() {
            return linkedModeHandler;
        }

        @Override
        public void setLinkedModeHandler(LinkedModeHandler handler) {
            this.linkedModeHandler = handler;
        }

        @Override
        public CaretPositionHandler getCaretPositionHandler() {
            return caretPositionHandler;
        }

        @Override
        public void setCaretPositionHandler(CaretPositionHandler handler) {
            this.caretPositionHandler = handler;
        }

        @Override
        public SelectionVisualHandler getSelectionVisualHandler() {
            return selectionVisualHandler;
        }

        @Override
        public void setSelectionVisualHandler(SelectionVisualHandler handler) {
            selectionVisualHandler = handler;
        }

        @Override
        public CaretPositionUndoHandler getCaretPositionUndoHandler() {
            return caretPositionUndoHandler;
        }

        @Override
        public void setCaretPositionUndoHandler(CaretPositionUndoHandler handler) {
            this.caretPositionUndoHandler = handler;
        }

        @Override
        public EclipseCommandHandler getEclipseCommandHandler() {
            return eclipseCommandHandler;
        }

        @Override
        public void setEclipseCommandHandler(EclipseCommandHandler handler) {
            this.eclipseCommandHandler = handler;
        }

        @Override
        public EclipsePlatform getPlatform() {
            return eclipsePlatform;
        }

        @Override
        public void setPlatform(EclipsePlatform platform) {
            eclipsePlatform = platform;
        }
    }
}
