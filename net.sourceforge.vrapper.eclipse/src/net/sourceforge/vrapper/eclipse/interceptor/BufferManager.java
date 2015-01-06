package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.vrapper.platform.VrapperPlatformException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * This singleton will assign each editor a unique id. The ids for active editors should remain
 * constant within the same Eclipse editing session, but it might change if Eclipse is restarted
 * (mainly due to inactive MultiPageEditors when Eclipse starts).
 */
public class BufferManager {
    
    public static final BufferManager INSTANCE = new BufferManager();

    /**
     * Buffer ids for all top-level editor references.
     * Note that some of these editor references might point to a MultiPageEditor, a fact which we
     * can't detect at startup time without forcing all editor plugins to load (we prefer being
     * wrong rather than slowing down Eclipse on startup due to forced loading of all plugins).
     * As a result, we might assign a single id to a MultiPageEditor when that id will be
     * invalidated later.
     */
    protected WeakHashMap<IEditorReference,BufferInfo> reservedBufferIdMapping =
            new WeakHashMap<IEditorReference, BufferInfo>();

    /**
     * Buffer ids for all active editors. Editors which aren't active are not included in this list,
     * whereas some active editors might be included in this list as well as the
     * {@link #reservedBufferIdMapping}
     */
    protected WeakHashMap<IEditorInput,BufferInfo> activeBufferIdMapping =
            new WeakHashMap<IEditorInput, BufferInfo>();

    protected final static AtomicInteger BUFFER_ID_SEQ = new AtomicInteger();

    public void registerEditorRef(IEditorReference ref) {
        if ( ! reservedBufferIdMapping.containsKey(ref)) {
            int bufferId = BUFFER_ID_SEQ.incrementAndGet();
            reservedBufferIdMapping.put(ref, new BufferInfo(bufferId, ref, ref.getId()));
        }
    }

    public void registerEditorPart(NestedEditorPartInfo nestingInfo, IEditorPart editorPart,
            boolean updateLastSeen) {
        IEditorInput input = editorPart.getEditorInput();
        IWorkbenchPage page = editorPart.getEditorSite().getPage();

        IWorkbenchPartReference reference;
        if (nestingInfo.getParentEditor().equals(editorPart)) {
            reference = page.getReference(editorPart);
        } else {
            reference = page.getReference(nestingInfo.getParentEditor());
        }
        // Remove any lingering references in case input was opened in two different editors.
        BufferInfo reservedBuffer = reservedBufferIdMapping.remove(reference);
        if ( ! activeBufferIdMapping.containsKey(input)) {
            int bufferId;
            BufferInfo info;
            String documentType;
            if (nestingInfo.getParentEditor().equals(editorPart)) {
                if (reservedBuffer == null) {
                    bufferId = BUFFER_ID_SEQ.incrementAndGet();
                } else {
                    bufferId = reservedBuffer.bufferId;
                }
                documentType = editorPart.getEditorSite().getId();
                info = new BufferInfo(bufferId, editorPart, input, documentType);
            } else {
                // Each child buffer gets its own id.
                bufferId = BUFFER_ID_SEQ.incrementAndGet();
                // Nested editors don't return reliable info, ask parent editor.
                IEditorInput parentInput = nestingInfo.getParentEditor().getEditorInput();
                documentType = nestingInfo.getParentEditor().getEditorSite().getId();
                info = new BufferInfo(bufferId, editorPart, parentInput, documentType, input);
            }
            activeBufferIdMapping.put(input, info);
        } else {
            // Verify if editorinput is still being edited in the same editor. It's possible that
            // a file is reopened in another editor, e.g. through "Open with" or a multipage editor.
            BufferInfo bufferInfo = activeBufferIdMapping.get(input);
            IEditorPart lastSeenEditor = null;
            if (bufferInfo.lastSeenEditor == null) {
                throw new VrapperPlatformException("LastSeenEditor weakref is null - this is a bug!");
            }
            lastSeenEditor = bufferInfo.lastSeenEditor.get();
            if ( ! editorPart.equals(lastSeenEditor) && updateLastSeen) {
                if (nestingInfo.getParentEditor().equals(editorPart)) {
                    bufferInfo.editorType = editorPart.getEditorSite().getId();
                    bufferInfo.parentInput = null;
                } else {
                    bufferInfo.editorType = nestingInfo.getParentEditor().getEditorSite().getId();
                    bufferInfo.parentInput = nestingInfo.getParentEditor().getEditorInput();
                }
                bufferInfo.lastSeenEditor = new WeakReference<IEditorPart>(editorPart);
            }
        }
    }

    public BufferInfo getBuffer(IEditorInput editorInput) {
        return activeBufferIdMapping.get(editorInput);
    }

    public List<BufferInfo> getBuffers() {
        SortedMap<Integer, BufferInfo> bufferMap = new TreeMap<Integer, BufferInfo>();
        for (BufferInfo refBuffer : reservedBufferIdMapping.values()) {
            bufferMap.put(refBuffer.bufferId, refBuffer);
        }
        for (BufferInfo inputBuffer : activeBufferIdMapping.values()) {
            bufferMap.put(inputBuffer.bufferId, inputBuffer);
        }
        return new ArrayList<BufferInfo>(bufferMap.values());
    }

    public static class BufferInfo {
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
        
        public void activate() {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (reference != null) {
                IEditorPart editor = reference.getEditor(true);
                if (editor == null) {
                    throw new VrapperPlatformException("Failed to activate editor for reference "
                            + reference);
                }
                page.activate(editor);
            } else if (input != null && parentInput == null) {
                try {
                    page.openEditor(input, editorType, true,
                            IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
                } catch (PartInitException e) {
                    throw new VrapperPlatformException("Failed to activate editor for input "
                        + input + ", type " + editorType, e);
                }
            } else if (input != null) {
                IEditorPart parentEditor;
                try {
                    parentEditor = page.openEditor(parentInput, editorType, false,
                            IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
                } catch (PartInitException e) {
                    throw new VrapperPlatformException("Failed to activate editor for input "
                        + input + ", type " + editorType, e);
                }
                if (parentEditor instanceof MultiPageEditorPart) {
                    MultiPageEditorPart multiPage = (MultiPageEditorPart) parentEditor;
                    IEditorPart[] foundEditors = multiPage.findEditors(input);
                    if (foundEditors.length > 0) {
                        multiPage.setActiveEditor(foundEditors[0]);
                    }
                } else if (parentEditor instanceof MultiEditor) {
                    MultiEditor editor = (MultiEditor) parentEditor;
                    IEditorPart[] innerEditors = editor.getInnerEditors();
                    int i = 0;
                    while (i < innerEditors.length
                            && ! input.equals(innerEditors[i].getEditorInput())) {
                        i++;
                    }
                    if (i < innerEditors.length) {
                        editor.activateEditor(innerEditors[i]);
                    }
                }
            } else {
                throw new VrapperPlatformException("Found bufferinfo object with no editor info!"
                        + " This is most likely a bug.");
            }
        }
    }
}
