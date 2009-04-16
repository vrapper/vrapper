package de.jroene.vrapper.eclipse.interceptor;

import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Just the {@link VerifyKeyListener} interface with a more suitable name
 * for our purpose. May be extended in the future.
 * @author Matthias Radig
 */
public interface InputInterceptor extends VerifyKeyListener {

    void partActivated(IWorkbenchPart part);

}
