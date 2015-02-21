package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.List;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

/**
 * The implementation will assign each editor a unique id. The ids for active editors should remain
 * constant within the same Eclipse editing session, but it might change if Eclipse is restarted
 * (mainly due to inactive MultiPageEditors when Eclipse starts).
 */
public interface BufferManager {
    
    /** Reserves a buffer id for the given {@link IEditorReference}. */
    public void registerEditorRef(IEditorReference ref);

    /**
     * Reserves a buffer id for the given {@link IEditorPart}. It will try to reuse the buffer id of
     * any registered {@link IEditorReference}. If the editor is a multi-page editor, buffer ids are
     * reserved for those inner editors.
     */
    public void registerEditorPart(EditorInfo editorInfo, boolean updateLastSeen);

    /** Find buffer information about the last editor which has shown given file input. */
    public BufferInfo getBuffer(IEditorInput editorInput);

    /**
     * Retrieve information about all known buffers. As noted on other methods, this includes
     * editors from the last Eclipse session and files which are no longer opened.
     * <p>Users should be aware that buffers for all windows are returned.
     */
    public List<BufferInfo> getBuffers();

    /**
     * Activate the editor in which the given buffer (file info) was last opened. This editor opens
     * in the currently active window.
     */
    public void activate(BufferInfo buffer);
}
