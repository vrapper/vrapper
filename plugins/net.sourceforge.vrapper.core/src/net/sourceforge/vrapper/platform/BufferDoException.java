package net.sourceforge.vrapper.platform;

import java.util.List;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class BufferDoException extends Exception {
    private static final long serialVersionUID = 2562623919242396240L;

    protected Buffer associatedBuffer;
    protected EditorAdaptor failedEditorAdaptor;
    protected List<Object> succeeded;

    public BufferDoException(List<Object> succeeded, EditorAdaptor failedVim, Throwable cause) {
        super(cause);
        this.succeeded = succeeded;
        this.failedEditorAdaptor = failedVim;
    }

    public BufferDoException(List<Object> succeeded, EditorAdaptor failedVim,
            Buffer associatedBuffer, Throwable cause) {
        super(cause);
        this.succeeded = succeeded;
        this.failedEditorAdaptor = failedVim;
        this.associatedBuffer = associatedBuffer;
    }

    public EditorAdaptor getCausingEditorAdaptor() {
        return failedEditorAdaptor;
    }

    public boolean isFailingEditorActivated() {
        return associatedBuffer != null;
    }

    /** Returns the activated buffer. This can be null. */
    public Buffer getActivatedBuffer() {
        return associatedBuffer;
    }

    public List<Object> getSucceeded() {
        return succeeded;
    }
}
