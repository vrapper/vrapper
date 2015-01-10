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
    
    public void registerEditorRef(IEditorReference ref);
    public void registerEditorPart(NestedEditorPartInfo nestingInfo, IEditorPart editorPart,
            boolean updateLastSeen);
    public BufferInfo getBuffer(IEditorInput editorInput);
    public List<BufferInfo> getBuffers();
    public void activate(BufferInfo buffer);
}
