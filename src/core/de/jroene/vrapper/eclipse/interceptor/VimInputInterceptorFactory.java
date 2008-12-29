package de.jroene.vrapper.eclipse.interceptor;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.jroene.vrapper.eclipse.EclipsePlatform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;

/**
 * A factory for interceptors which route input events to a {@link VimEmulator}
 * instance. This instance decides whether to pass the event to the underlying
 * editor or not.
 *
 * @author Matthias Radig
 */
public class VimInputInterceptorFactory implements InputInterceptorFactory {

    private static final HashMap<Integer, VimInputEvent> specialKeys;
    static {
        specialKeys = new HashMap<Integer, VimInputEvent>();
        specialKeys.put( KeyEvent.VK_ESCAPE,     VimInputEvent.ESCAPE);
        specialKeys.put( SWT.ARROW_LEFT,         VimInputEvent.ARROW_LEFT);
        specialKeys.put( SWT.ARROW_RIGHT,        VimInputEvent.ARROW_RIGHT);
        specialKeys.put( SWT.ARROW_UP,           VimInputEvent.ARROW_UP);
        specialKeys.put( SWT.ARROW_DOWN,         VimInputEvent.ARROW_DOWN);
        specialKeys.put( KeyEvent.VK_BACK_SPACE, VimInputEvent.BACKSPACE);
        specialKeys.put( (int)SWT.CR,            VimInputEvent.RETURN);
        specialKeys.put( KeyEvent.VK_DELETE,     VimInputEvent.DELETE);
        specialKeys.put( KeyEvent.VK_INSERT,     VimInputEvent.INSERT);
        specialKeys.put( KeyEvent.VK_PAGE_DOWN,  VimInputEvent.PAGE_DOWN);
        specialKeys.put( KeyEvent.VK_PAGE_UP,    VimInputEvent.PAGE_UP);
        specialKeys.put( KeyEvent.VK_HOME,       VimInputEvent.HOME);
        specialKeys.put( KeyEvent.VK_END,        VimInputEvent.END);
    }
    public InputInterceptor createInterceptor(final IWorkbenchWindow window,
            final AbstractTextEditor part, final ITextViewer textViewer) {
        return new InputInterceptor() {

            private final VimEmulator vim = new VimEmulator(
                    new EclipsePlatform(window, part, textViewer));
            public void verifyKey(VerifyEvent e) {
                VimInputEvent in;
                if(e.keyCode == SWT.SHIFT) {
                    return;
                }
                if(specialKeys.containsKey(e.keyCode)) {
                    in = specialKeys.get(e.keyCode);
                } else {
                    in = new VimInputEvent.Character(e.character);
                }
                e.doit = vim.type(in);
            }

        };
    }

}
