package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.swt.custom.VerifyKeyListener;

/**
 * Just the {@link VerifyKeyListener} interface with a more suitable name
 * for our purpose. May be extended in the future.
 * @author Matthias Radig
 */
public interface InputInterceptor extends VerifyKeyListener {
    public EditorAdaptor getEditorAdaptor();

}
