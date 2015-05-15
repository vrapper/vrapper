
package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.HashMap;
import java.util.HashSet;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.eclipse.platform.SWTRegisterManager;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.GlobalConfiguration;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.SimpleGlobalConfiguration;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

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
    static {
        specialKeys = new HashMap<Integer, SpecialKey>();
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

        /*
         * Translate the "Escape characters" sent by Ctrl + alpha keys to regular characters.
         * This circumvents problems with the user language where raw keyCode values might not.
         */
        escapedChars = new HashMap<Character, Character>();
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

        ignoredKeyCodes = new HashSet<Integer>();
        ignoredKeyCodes.add(SWT.CTRL);
        ignoredKeyCodes.add(SWT.SHIFT);
        ignoredKeyCodes.add(SWT.ALT);
        ignoredKeyCodes.add(SWT.CAPS_LOCK);
        ignoredKeyCodes.add(SWT.COMMAND);
    }


    private static final GlobalConfiguration sharedConfiguration = new SimpleGlobalConfiguration();
    private static final RegisterManager globalRegisterManager = new SWTRegisterManager(
            PlatformUI.getWorkbench().getDisplay(), sharedConfiguration);

    public InputInterceptor createInterceptor(AbstractTextEditor abstractTextEditor,
            ITextViewer textViewer, EditorInfo partInfo, BufferAndTabService bufferAndTabService) {
        EclipsePlatform platform = new EclipsePlatform(partInfo, abstractTextEditor, textViewer,
                sharedConfiguration, bufferAndTabService);
        DefaultEditorAdaptor editorAdaptor = new DefaultEditorAdaptor(
                platform,
                globalRegisterManager, VrapperPlugin.isVrapperEnabled());
        InputInterceptor interceptor = createInterceptor(editorAdaptor);
        interceptor.setEditorInfo(partInfo);
        interceptor.setCaretPositionUndoHandler(new CaretPositionUndoHandler(editorAdaptor, textViewer));
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
        SelectionVisualHandler visualHandler = new SelectionVisualHandler(editorAdaptor,
                platform.getSelectionService(), textViewer);
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

        private VimInputInterceptor(EditorAdaptor editorAdaptor) {
            this.editorAdaptor = editorAdaptor;
        }

        public void verifyKey(VerifyEvent event) {
            if (!VrapperPlugin.isVrapperEnabled()) {
                return;
            }
            if (ignoredKeyCodes.contains(event.keyCode)) {
                return;
            }
            KeyStroke keyStroke;
            boolean shiftKey = (event.stateMask & SWT.SHIFT) != 0;
            boolean altKey   = (event.stateMask & SWT.ALT)   != 0;
            boolean ctrlKey   = (event.stateMask & SWT.CONTROL | event.stateMask & SWT.COMMAND)   != 0;
            if(specialKeys.containsKey(event.keyCode)) {
                keyStroke = new SimpleKeyStroke(specialKeys.get(event.keyCode), shiftKey, altKey, ctrlKey);
            } else if (escapedChars.containsKey(event.character)) {
                keyStroke = new SimpleKeyStroke(escapedChars.get(event.character), shiftKey, altKey, ctrlKey);
            } else {
                keyStroke = new SimpleKeyStroke(event.character, shiftKey, altKey, ctrlKey);
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
    }
}
