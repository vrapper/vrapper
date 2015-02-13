package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.interceptor.BufferInfo;
import net.sourceforge.vrapper.eclipse.interceptor.BufferManager;
import net.sourceforge.vrapper.eclipse.interceptor.NestedEditorPartInfo;
import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.BufferDoException;
import net.sourceforge.vrapper.platform.Tab;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;

public class EclipseBufferAndTabService implements BufferAndTabService {
    protected IEditorPart previousEditor;
    protected IEditorPart currentEditor;
    protected BufferManager bufferIdManager;
    protected IWorkbenchWindow workbenchWindow;
    
    public EclipseBufferAndTabService(IWorkbenchWindow window, BufferManager bufferIdManager) {
        this.workbenchWindow = window;
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
        if (activeEditor == null || activeEditor.getEditorInput() == null) {
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
        bufferIdManager.activate(targetBuffer.bufferInfo);
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
            // Filter list so only buffers in current window are shown.
            if ( ! editorInfo.seenWindows.containsKey(workbenchWindow)) {
                continue;
            }
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
     * @see net.sourceforge.vrapper.eclipse.platform.BufferAndTabService#doInBuffers(..)
     */
    @Override
    public List<Object> doInBuffers(boolean initialize, Queue<String> command, Evaluator code) throws BufferDoException {
        if (initialize) {
            // NOTE: This is can be costly operation if you have 10+ files open from last session.
            // Since all editors will be initialized, all Vrapper instances will be in buffer list.
            IEditorReference[] references = workbenchWindow.getActivePage().getEditorReferences();
            for (IEditorReference ref : references) {
                ref.getEditor(true);
            }
        }
        List<Object> result = new ArrayList<Object>();
        Map<IEditorPart, EditorAdaptor> knownEditors = VrapperPlugin.getDefault().getKnownEditors();
        for (Map.Entry<IEditorPart, EditorAdaptor> editorInfo : knownEditors.entrySet()) {
            IEditorPart editor = editorInfo.getKey();
            // Global list contains editors from other windows as well; filter those.
            if (editor == null || editor.getSite() == null
                    || ! workbenchWindow.equals(editor.getSite().getWorkbenchWindow())) {
                continue;
            }
            EditorAdaptor editorAdaptor = editorInfo.getValue();
            result.add(code.evaluate(editorAdaptor, command));
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
