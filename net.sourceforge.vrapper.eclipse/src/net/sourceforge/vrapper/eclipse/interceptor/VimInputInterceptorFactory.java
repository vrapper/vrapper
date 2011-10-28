
package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.HashMap;
import java.util.HashSet;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
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


    private static final RegisterManager globalRegisterManager = new DefaultRegisterManager();

    public InputInterceptor createInterceptor(AbstractTextEditor abstractTextEditor, ITextViewer textViewer) {
        EditorAdaptor editorAdaptor = new DefaultEditorAdaptor(
                new EclipsePlatform(abstractTextEditor, textViewer),
                globalRegisterManager, VrapperPlugin.isVrapperEnabled());
        return new VimInputInterceptor(editorAdaptor);
    }

    private static final class VimInputInterceptor implements InputInterceptor {

        private final EditorAdaptor editorAdaptor;

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
            if(specialKeys.containsKey(event.keyCode)) {
                keyStroke = new SimpleKeyStroke(specialKeys.get(event.keyCode));
            } else if (specialChars.containsKey(event.character)) {
                keyStroke = new SimpleKeyStroke(specialChars.get(event.character));
            } else {
                keyStroke = new SimpleKeyStroke(event.character);
            }
            event.doit = !editorAdaptor.handleKey(keyStroke);
        }

        public EditorAdaptor getEditorAdaptor() {
            return editorAdaptor;
        }

    }
};