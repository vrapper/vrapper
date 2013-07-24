
package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.HashMap;
import java.util.HashSet;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.eclipse.platform.SWTRegisterManager;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SimpleConfiguration;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
    private static final HashMap<Character, SpecialKey> specialChars;
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
        
        SpecialKey[] values = SpecialKey.values();
        int swtStart = SWT.F1;
        int skStart = SpecialKey.F1.ordinal();
        //SWT has up to F20
        for (int i=0; i < 20; ++i)
        	specialKeys.put(swtStart+i, values[skStart+i]);

        specialChars = new HashMap<Character, SpecialKey>();
        specialChars.put(Character.valueOf('\n'), SpecialKey.RETURN);
        specialChars.put(Character.valueOf('\r'), SpecialKey.RETURN);
        specialChars.put(Character.valueOf('\u001B'), SpecialKey.ESC);

        ignoredKeyCodes = new HashSet<Integer>();
        ignoredKeyCodes.add(SWT.CTRL);
        ignoredKeyCodes.add(SWT.SHIFT);
        ignoredKeyCodes.add(SWT.ALT);
        ignoredKeyCodes.add(SWT.CAPS_LOCK);
    }


    private static final RegisterManager globalRegisterManager = new SWTRegisterManager(PlatformUI.getWorkbench().getDisplay());
    private static final Configuration sharedConfiguration = new SimpleConfiguration();

    public InputInterceptor createInterceptor(AbstractTextEditor abstractTextEditor, ITextViewer textViewer) {
        EclipsePlatform platform = new EclipsePlatform(abstractTextEditor, textViewer, sharedConfiguration);
        DefaultEditorAdaptor editorAdaptor = new DefaultEditorAdaptor(
                platform,
                globalRegisterManager, VrapperPlugin.isVrapperEnabled());
        InputInterceptor interceptor = createInterceptor(editorAdaptor);
        if (editorAdaptor.getConfiguration().get(Options.EXIT_LINK_MODE)) {
            LinkedModeHandler linkedModeHandler = new LinkedModeHandler(editorAdaptor);
            LinkedModeHandler.registerListener(textViewer.getDocument(), linkedModeHandler);
            interceptor.setLinkedModeHandler(linkedModeHandler);
        }
        return interceptor;
    }

    public InputInterceptor createInterceptor(EditorAdaptor editorAdaptor) {
        return new VimInputInterceptor(editorAdaptor);
    }

    private static final class VimInputInterceptor implements InputInterceptor {

        private final EditorAdaptor editorAdaptor;
        private LinkedModeHandler linkedModeHandler;

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
            boolean ctrlKey   = (event.stateMask & SWT.CONTROL)   != 0;
            if(specialKeys.containsKey(event.keyCode)) {
                keyStroke = new SimpleKeyStroke(specialKeys.get(event.keyCode), shiftKey, altKey, ctrlKey);
            } else if (specialChars.containsKey(event.character)) {
                keyStroke = new SimpleKeyStroke(specialChars.get(event.character), shiftKey, altKey, ctrlKey);
            } else {
                keyStroke = new SimpleKeyStroke(event.character, shiftKey, altKey, ctrlKey);
            }
            event.doit = !editorAdaptor.handleKey(keyStroke);
        }

        public EditorAdaptor getEditorAdaptor() {
            return editorAdaptor;
        }
        
		public void selectionChanged(SelectionChangedEvent event) {
			if (!VrapperPlugin.isMouseDown()
					|| !(event.getSelection() instanceof TextSelection)
					|| !editorAdaptor.getConfiguration().get(Options.VISUAL_MOUSE))
				return;
			
        	TextSelection selection = (TextSelection) event.getSelection();
        	// selection.isEmpty() is false even if length == 0, don't use it
        	if (selection instanceof TextSelection) {
				if (selection.getLength() == 0 &&
						(VisualMode.NAME.equals(editorAdaptor.getCurrentModeName())
							|| LinewiseVisualMode.NAME.equals(editorAdaptor.getCurrentModeName()))) {
					editorAdaptor.changeModeSafely(NormalMode.NAME);
				} else if(selection.getLength() != 0 && NormalMode.NAME.equals(editorAdaptor.getCurrentModeName())) {
					 /* gvim supports switching to visual from insert but we
					 * don't allow it because it might interfere with
					 * functionality of the underlying editor */
		    		editorAdaptor.changeModeSafely(VisualMode.NAME, AbstractVisualMode.KEEP_SELECTION_HINT);
				}
	        }
		}

        @Override
        public LinkedModeHandler getLinkedModeHandler() {
            return linkedModeHandler;
        }

        @Override
        public void setLinkedModeHandler(LinkedModeHandler handler) {
            this.linkedModeHandler = handler;
        }
    }
}
