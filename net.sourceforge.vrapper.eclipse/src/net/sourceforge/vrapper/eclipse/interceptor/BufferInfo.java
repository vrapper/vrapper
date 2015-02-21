package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;

public class BufferInfo {
    public final int bufferId;
    public IEditorInput input;
    public final IEditorReference reference;
    public String editorType;
    public IEditorInput parentInput;
    /**
     * Editor in which this buffer was last seen. This is only used to know if the editor could have
     * changed to a multi-page editor, in which case we need to do additional checks.
     * <p>
     * This assumes the user doesn't frequently switches windows from the same Eclipse session and
     * that he doesn't frequently switch between duplicated editors.
     */
    public WeakReference<IEditorPart> lastSeenEditor;
    public WeakHashMap<IWorkbenchWindow, Void> seenWindows = new WeakHashMap<IWorkbenchWindow, Void>();

    protected BufferInfo(int bufferId, IEditorPart editorPart, IEditorInput input, String documentType) {
        this(bufferId, editorPart, null, documentType, input);
    }

    public BufferInfo(int bufferId, IEditorPart editorPart, IEditorInput parentInput,
            String documentType, IEditorInput subResourceInput) {
        this.bufferId = bufferId;
        this.parentInput = parentInput;
        this.input = subResourceInput;
        this.editorType = documentType;
        this.reference = null;
        this.lastSeenEditor = new WeakReference<IEditorPart>(editorPart);
        this.seenWindows.put(editorPart.getEditorSite().getWorkbenchWindow(), null);
    }

    public BufferInfo(int bufferId, IEditorReference editorReference, String pluginId) {
        this.bufferId = bufferId;
        this.parentInput = null;
        this.input = null;
        this.editorType = pluginId;
        this.reference = editorReference;
        this.seenWindows.put(editorReference.getPage().getWorkbenchWindow(), null);
    }

    public String getDisplayName() {
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput) input;
            return fileInput.getFile().getFullPath().toFile().getPath();
        } else if (input != null) {
            return input.getName();
        } else if (reference != null) {
            return reference.getPartName();
        } else {
            return "?";
        }
    }
}