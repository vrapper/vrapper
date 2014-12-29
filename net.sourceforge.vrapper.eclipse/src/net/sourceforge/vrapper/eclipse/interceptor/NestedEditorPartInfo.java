package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IEditorPart;

/**
 * This class holds information on the way top-level editors are nested.
 * <p>
 * Certain editors consist of multiple pages (e.g. the PDE Plugin editor), we need to keep this
 * information to prevent endless recursion when installing Vrapper into all the child editors. This
 * information is also necessary when switching between different child editors.
 */
public class NestedEditorPartInfo {

    protected Map<IEditorPart, Void> childEditors = new IdentityHashMap<IEditorPart, Void>();
    protected IEditorPart parent;

    public NestedEditorPartInfo(IEditorPart parent) {
        this.parent = parent;
    }

    public void addChildEditor(IEditorPart editorPart) {
        this.childEditors.put(editorPart, null);
    }

    public Set<IEditorPart> getChildEditors() {
        return childEditors.keySet();
    }

    public void setChildEditors(Set<IEditorPart> childEditors) {
        this.childEditors = new IdentityHashMap<IEditorPart, Void>();
        for (IEditorPart editor : childEditors) {
            this.childEditors.put(editor, null);
        }
    }

    public boolean containedInTree(IEditorPart editorPart) {
        return this.parent.equals(editorPart) || this.childEditors.containsKey(editorPart);
    }

    public IEditorPart getParentEditor() {
        return parent;
    }

    public void setParentEditor(IEditorPart parent) {
        this.parent = parent;
    }
}
