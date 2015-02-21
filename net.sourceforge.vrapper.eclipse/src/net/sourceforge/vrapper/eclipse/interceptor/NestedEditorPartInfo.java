package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.vrapper.platform.VrapperPlatformException;

import org.eclipse.ui.IEditorPart;

/**
 * This class holds information on the way top-level editors are nested.
 * <p>
 * Certain editors consist of multiple pages (e.g. the PDE Plugin editor), we need to keep this
 * information to prevent endless recursion when installing Vrapper into all the child editors. This
 * information is also necessary when switching between different child editors.
 * <p>
 * <b>Note</b>: this keeps hard references to Editors, instances of this class are not meant to be
 * stored over a long period of time unless the lifetime is tied to the editor parts themselves.
 */
public class NestedEditorPartInfo {

    protected Map<IEditorPart, NestedEditorPartInfo> childEditors = new IdentityHashMap<IEditorPart, NestedEditorPartInfo>();
    protected NestedEditorPartInfo parent;
    protected IEditorPart current;

    protected NestedEditorPartInfo(IEditorPart current) {
        this.current = current;
    }

    protected NestedEditorPartInfo(NestedEditorPartInfo parent, IEditorPart current) {
        this.parent = parent;
        this.current = current;
    }

    /** Stores information about the child and returns the NestedEditorpartInfo for the child. */
    public NestedEditorPartInfo createChildInfo(IEditorPart editorPart) {
        if (editorPart == null) {
            throw new VrapperPlatformException("Child editor cannot be null! Error while"
                    + " traversing " + current);
        }
        NestedEditorPartInfo child = new NestedEditorPartInfo(this, editorPart);
        this.childEditors.put(editorPart, child);
        return child;
    }

    public Map<IEditorPart, NestedEditorPartInfo> getChildEditors() {
        return Collections.unmodifiableMap(childEditors);
    }

    public void setChildEditors(Set<IEditorPart> childEditors) {
        this.childEditors = new IdentityHashMap<IEditorPart, NestedEditorPartInfo>();
        for (IEditorPart editor : childEditors) {
            this.childEditors.put(editor, new NestedEditorPartInfo(editor));
        }
    }

    public NestedEditorPartInfo getChild(IEditorPart subPart) {
        NestedEditorPartInfo childInfo = childEditors.get(subPart);
        if (childInfo == null) {
            throw new VrapperPlatformException("Corrupt editor info structure found for editor "
                    + current);
        }
        return childInfo;
    }

    public boolean isTopLevelEditor() {
        return parent == null;
    }

    /** Whether this editor is a simple AbstractTextEditor without any nested parts. */
    public boolean isSimpleEditor() {
        return parent == null && childEditors.isEmpty();
    }

    public IEditorPart getTopLevelEditor() {
        if (parent == null) {
            return current;
        } else {
            return parent.getTopLevelEditor();
        }
    }

    public NestedEditorPartInfo getParentInfo() {
        return parent;
    }

    public IEditorPart getCurrent() {
        return current;
    }
}
