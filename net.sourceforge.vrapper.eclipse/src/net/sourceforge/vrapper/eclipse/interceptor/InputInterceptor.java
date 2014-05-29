package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.VerifyKeyListener;

/**
 * Just the {@link VerifyKeyListener} interface with a more suitable name
 * for our purpose. May be extended in the future.
 * @author Matthias Radig
 */
public interface InputInterceptor extends VerifyKeyListener, ISelectionChangedListener {
    public EditorAdaptor getEditorAdaptor();

    public LinkedModeHandler getLinkedModeHandler();
    public void setLinkedModeHandler(LinkedModeHandler handler);
    
    public CaretPositionHandler getCaretPositionHandler();
    public void setCaretPositionHandler(CaretPositionHandler handler);
}
