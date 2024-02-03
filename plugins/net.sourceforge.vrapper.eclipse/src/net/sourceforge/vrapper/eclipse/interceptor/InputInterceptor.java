package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.platform.EclipsePlatform;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.swt.custom.VerifyKeyListener;

/**
 * Just the {@link VerifyKeyListener} interface with a more suitable name
 * for our purpose. May be extended in the future.
 * @author Matthias Radig
 */
public interface InputInterceptor extends VerifyKeyListener {
    public EditorAdaptor getEditorAdaptor();

    public EditorInfo getEditorInfo();
    void setEditorInfo(EditorInfo partInfo);

    public LinkedModeHandler getLinkedModeHandler();
    public void setLinkedModeHandler(LinkedModeHandler handler);
    
    public CaretPositionHandler getCaretPositionHandler();
    public void setCaretPositionHandler(CaretPositionHandler handler);
    
    public SelectionVisualHandler getSelectionVisualHandler();
    public void setSelectionVisualHandler(SelectionVisualHandler handler);
    
    public CaretPositionUndoHandler getCaretPositionUndoHandler();
    public void setCaretPositionUndoHandler(CaretPositionUndoHandler handler);

    public EclipsePlatform getPlatform();
    public void setPlatform(EclipsePlatform platform);
}
