package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.ui.IWorkbenchPart;

/**
 * This helper class holds information to make sure that an editor is not processed indefinitely
 * while recursively stepping through a part tree.  
 */
public class ProcessedInfo {
    protected Map<IWorkbenchPart, Void> markedParts = new IdentityHashMap<IWorkbenchPart, Void>();
    
    public ProcessedInfo(IWorkbenchPart root) {
        markedParts.put(root, null);
    }
    
    public boolean isProcessed(IWorkbenchPart part) {
        return markedParts.containsKey(part);
    }
    
    public ProcessedInfo markPart(IWorkbenchPart part) {
        markedParts.put(part, null);
        return this;
    }
}
