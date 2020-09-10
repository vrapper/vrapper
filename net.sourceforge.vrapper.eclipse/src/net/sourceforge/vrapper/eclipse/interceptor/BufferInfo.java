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
    public final IEditorReference reference;
    public String editorType;
    
    /**
     * We use WeakReferences for IEditorInputs because BufferInfo is used for values
     * in the WeakHashMap {@link InputInterceptorManager#activeBufferIdMapping} and
     * values in a WeakHashMap should not have strong references to the keys of the
     * WeakHashMap, otherwise that prevents keys from being discarded through garbage
     * collection.
     * 
     * From the User's perspective, using strong references here can prevent an
     * IEditorInput object from being garbage collected long after its editor
     * has been closed. That can lead to memory exhaustion when the IEditorInput
     * holds large data objects.
     */
    protected WeakReference<IEditorInput> parentInput;
    protected WeakReference<IEditorInput> input;

    /**
     * Editor in which this buffer was last seen. This is only used to know if the editor could have
     * changed to a multi-page editor, in which case we need to do additional checks.
     * <p>
     * This assumes the user doesn't frequently switches windows from the same Eclipse session and
     * that he doesn't frequently switch between duplicated editors.
     */
    public WeakReference<IEditorPart> lastSeenEditor;
    public WeakHashMap<IWorkbenchWindow, Void> seenWindows = new WeakHashMap<IWorkbenchWindow, Void>();

    public BufferInfo(int bufferId, IEditorPart editorPart, IEditorInput parentInput,
            String documentType, IEditorInput subResourceInput) {
        this.bufferId = bufferId;
        this.parentInput = new WeakReference<IEditorInput>(parentInput);
        this.input = new WeakReference<IEditorInput>(subResourceInput);
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
    	final IEditorInput input = getInput();
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
    
    public IEditorInput getInput() {
    	return input.get();
    }

    public IEditorInput getParentInput() {
    	return parentInput.get();
    }
}