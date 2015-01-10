package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.ref.WeakReference;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.FileEditorInput;

public class BufferInfo {
    public final int bufferId;
    public IEditorInput input;
    public final IEditorReference reference;
    public String editorType;
    public IEditorInput parentInput;
    public WeakReference<IEditorPart> lastSeenEditor;

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
    }

    public BufferInfo(int bufferId, IEditorReference editorReference, String pluginId) {
        this.bufferId = bufferId;
        this.parentInput = null;
        this.input = null;
        this.editorType = pluginId;
        this.reference = editorReference;
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