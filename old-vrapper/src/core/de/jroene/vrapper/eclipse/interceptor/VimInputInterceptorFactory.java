package de.jroene.vrapper.eclipse.interceptor;

import java.util.HashMap;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.jroene.vrapper.eclipse.EclipsePlatform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;
import de.jroene.vrapper.vim.register.DefaultRegisterManager;
import de.jroene.vrapper.vim.register.RegisterManager;

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
        specialKeys.put( (int)SWT.ESC,           VimInputEvent.ESCAPE);
        specialKeys.put( SWT.ARROW_LEFT,         VimInputEvent.ARROW_LEFT);
        specialKeys.put( SWT.ARROW_RIGHT,        VimInputEvent.ARROW_RIGHT);
        specialKeys.put( SWT.ARROW_UP,           VimInputEvent.ARROW_UP);
        specialKeys.put( SWT.ARROW_DOWN,         VimInputEvent.ARROW_DOWN);
        specialKeys.put( (int)SWT.BS,            VimInputEvent.BACKSPACE);
        specialKeys.put( (int)SWT.CR,            VimInputEvent.RETURN);
        specialKeys.put( (int)SWT.DEL,           VimInputEvent.DELETE);
        specialKeys.put( SWT.INSERT,             VimInputEvent.INSERT);
        specialKeys.put( SWT.PAGE_DOWN,          VimInputEvent.PAGE_DOWN);
        specialKeys.put( SWT.PAGE_UP,            VimInputEvent.PAGE_UP);
        specialKeys.put( SWT.HOME,               VimInputEvent.HOME);
        specialKeys.put( SWT.END,                VimInputEvent.END);
    }

    private static final RegisterManager globalRegisterManager = new DefaultRegisterManager();

    public InputInterceptor createInterceptor(final IWorkbenchWindow window,
            final AbstractTextEditor part, final ITextViewer textViewer) {
        return new InputInterceptor() {

            private final EclipsePlatform platform = new EclipsePlatform(window, part, textViewer);
            private final VimEmulator vim = new VimEmulator(
                    platform, globalRegisterManager);
            public void verifyKey(VerifyEvent e) {
                VimInputEvent in;
                if(e.keyCode == SWT.SHIFT || e.keyCode == SWT.CTRL) {
                    return;
                }
                if(specialKeys.containsKey(e.keyCode)) {
                    in = specialKeys.get(e.keyCode);
                } else {
                    in = new VimInputEvent.Character(e.character);
                }
                e.doit = vim.type(in);
            }

            public void partActivated(IWorkbenchPart arg0) {
                platform.activate();
            }

        };
    }

}
