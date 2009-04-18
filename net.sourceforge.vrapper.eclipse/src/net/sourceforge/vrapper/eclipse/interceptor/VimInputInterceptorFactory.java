package net.sourceforge.vrapper.eclipse.interceptor;

import static net.sourceforge.vrapper.keymap.KeyStroke.ALT;
import static net.sourceforge.vrapper.keymap.KeyStroke.CTRL;
import static net.sourceforge.vrapper.keymap.KeyStroke.SHIFT;

import java.util.HashMap;

import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * A factory for interceptors which route input events to a {@link VimEmulator}
 * instance. This instance decides whether to pass the event to the underlying
 * editor or not.
 *
 * @author Matthias Radig
 */
public class VimInputInterceptorFactory implements InputInterceptorFactory {

    private static final HashMap<Integer, SpecialKey> specialKeys;
    static {
        specialKeys = new HashMap<Integer, SpecialKey>();
        specialKeys.put( (int)SWT.ESC,           SpecialKey.ESC);
        specialKeys.put( SWT.ARROW_LEFT,         SpecialKey.ARROW_LEFT);
        specialKeys.put( SWT.ARROW_RIGHT,        SpecialKey.ARROW_RIGHT);
        specialKeys.put( SWT.ARROW_UP,           SpecialKey.ARROW_UP);
        specialKeys.put( SWT.ARROW_DOWN,         SpecialKey.ARROW_DOWN);
        specialKeys.put( (int)SWT.BS,            SpecialKey.BACKSPACE);
        specialKeys.put( (int)SWT.CR,            SpecialKey.RETURN);
        specialKeys.put( (int)SWT.DEL,           SpecialKey.DELETE);
        specialKeys.put( SWT.INSERT,             SpecialKey.INSERT);
        specialKeys.put( SWT.PAGE_DOWN,          SpecialKey.PAGE_DOWN);
        specialKeys.put( SWT.PAGE_UP,            SpecialKey.PAGE_UP);
        specialKeys.put( SWT.HOME,               SpecialKey.HOME);
        specialKeys.put( SWT.END,                SpecialKey.END);
    }

    private static final RegisterManager globalRegisterManager = new DefaultRegisterManager();

    public InputInterceptor createInterceptor(final IWorkbenchWindow window, final AbstractTextEditor abstractTextEditor, final ITextViewer textViewer) {

    	final EditorAdaptor editorAdaptor = new DefaultEditorAdaptor(
    			new EclipsePlatform(abstractTextEditor, textViewer),
    			globalRegisterManager);

    	return new InputInterceptor() {

            public void verifyKey(VerifyEvent event) {
            	int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
            	org.eclipse.jface.bindings.keys.KeyStroke jfaceKS = SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
            	if (!jfaceKS.isComplete())
            		return;
            	int modifiers = convertModifiers(event.stateMask);
            	char character = convertCharacter(modifiers, event.character);
            	SpecialKey key = null;
            	KeyStroke keyStroke = null;
                if (specialKeys.containsKey(event.keyCode)) {
					key = specialKeys.get(event.keyCode);
					keyStroke = new SimpleKeyStroke(modifiers, key);
                } else
                	keyStroke = new SimpleKeyStroke(modifiers, character);
                event.doit = !editorAdaptor.handleKey(keyStroke);
            }

			@Override
			public void partActivated(IWorkbenchPart part) {
				// ???
			}

        };
    }

	protected char convertCharacter(int modifiers, char character) {
		if (0 <= character && character <= 0x1F && (modifiers & KeyStroke.CTRL) != 0)
			character += 0x40;
		if (Character.isLetter(character) && (modifiers & KeyStroke.SHIFT) == 0)
			character = Character.toLowerCase(character);
		return character;
	}

	protected int convertModifiers(int stateMask) {
		int result = 0;
		if ((stateMask & SWT.CTRL) != 0) result |= CTRL;
		if ((stateMask & SWT.ALT) != 0) result |= ALT;
		if ((stateMask & SWT.SHIFT) != 0) result |= SHIFT;
		return result;
	}

}
