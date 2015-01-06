package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.eclipse.interceptor.BufferManager;
import net.sourceforge.vrapper.eclipse.interceptor.BufferManager.BufferInfo;
import net.sourceforge.vrapper.eclipse.interceptor.NestedEditorPartInfo;
import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.Tab;
import net.sourceforge.vrapper.platform.VrapperPlatformException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class EclipseBufferAndTabService implements BufferAndTabService {
    protected IEditorPart previousEditor;
    protected IEditorPart currentEditor;
    protected BufferManager bufferIdManager;
    
    public EclipseBufferAndTabService(BufferManager bufferIdManager) {
        this.bufferIdManager = bufferIdManager;
    }

    /** Thin wrapper around the {@link BufferInfo} class to store extra flags. */
    public static class EclipseBuffer implements Buffer {
        private BufferInfo bufferInfo;
        private boolean isActive;
        private boolean isAlternate;

        public EclipseBuffer(BufferInfo bufferInfo) {
            this.bufferInfo = bufferInfo;
        }
        
        public EclipseBuffer(EclipseBuffer original) {
            this.bufferInfo = original.bufferInfo;
        }

        @Override
        public int getId() {
            return bufferInfo.bufferId;
        }

        @Override
        public String getDisplayName() {
            return bufferInfo.getDisplayName();
        }

        @Override
        public boolean isAlternate() {
            return isAlternate;
        }

        @Override
        public boolean isActive() {
            return isActive;
        }

        public EclipseBuffer markActive() {
            this.isActive = true;
            return this;
        }

        public EclipseBuffer markAlternate() {
            this.isAlternate = true;
            return this;
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof Buffer) && bufferInfo.bufferId == ((Buffer)other).getId();
        }
    }

    public void setCurrentEditor(NestedEditorPartInfo nestingInfo, IEditorPart activeEditor) {
        if (activeEditor == null) {
            return;
        }
        if (currentEditor == null) {
            previousEditor = activeEditor;
        } else if ( ! currentEditor.equals(activeEditor)
                && ! currentEditor.getEditorInput().equals(activeEditor.getEditorInput())) {
            // Only replace previous editor info when we switched to a different editor and input.
            previousEditor = currentEditor;
        }
        currentEditor = activeEditor;
        // Update IEditorPart info.
        bufferIdManager.registerEditorPart(nestingInfo, activeEditor, true);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#getPreviousBuffer()
     */
    @Override
    public Buffer getPreviousBuffer() {
        if (previousEditor == null) {
            throw new VrapperPlatformException("Previous editor not set - cannot continue.");
        }
        IEditorInput previousInput = previousEditor.getEditorInput();
        BufferInfo bufferInfo = bufferIdManager.getBuffer(previousInput);
        // There is no alternate buffer yet.
        if (previousEditor.equals(currentEditor)) {
            return new EclipseBuffer(bufferInfo).markActive();
        } else {
            return new EclipseBuffer(bufferInfo).markAlternate();
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#getActiveBuffer()
     */
    @Override
    public Buffer getActiveBuffer() {
        if (currentEditor == null) {
            throw new VrapperPlatformException("Current editor not set - cannot continue.");
        }
        IEditorInput currentInput = currentEditor.getEditorInput();
        BufferInfo bufferInfo = bufferIdManager.getBuffer(currentInput);
        return new EclipseBuffer(bufferInfo).markActive();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#switchBuffer(net.sourceforge.vrapper.platform.Buffer)
     */
    @Override
    public void switchBuffer(Buffer buffer) {
        if ( ! (buffer instanceof EclipseBuffer)) {
            throw new VrapperPlatformException("Received an unexpected kind of Buffer object!");
        }
        EclipseBuffer targetBuffer = (EclipseBuffer) buffer;
        targetBuffer.bufferInfo.activate();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#getBuffers()
     */
    @Override
    public List<Buffer> getBuffers() {
        List<Buffer> result = new ArrayList<Buffer>();
        List<BufferInfo> buffers = bufferIdManager.getBuffers();
        if (currentEditor == null || buffers.size() == 0) {
            return Collections.emptyList();
        }
        IEditorInput currentInput = currentEditor.getEditorInput();
        IEditorInput previousInput = previousEditor.getEditorInput();
        for (BufferInfo editorInfo : buffers) {
            EclipseBuffer eclipseBuffer = new EclipseBuffer(editorInfo);
            if (currentInput.equals(editorInfo.input)) {
                eclipseBuffer.markActive();
            } else if (previousInput.equals(editorInfo.input)) {
                eclipseBuffer.markAlternate();
            }
            result.add(eclipseBuffer);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#getActiveTab()
     */
    @Override
    public Tab getActiveTab() {
        // TODO
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#getTabs()
     */
    @Override
    public List<Tab> getTabs() {
        // TODO
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#switchTab(net.sourceforge.vrapper.platform.Tab)
     */
    @Override
    public void switchTab(Tab tab) {
        // TODO
    }
}
