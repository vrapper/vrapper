package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.extractor.EditorExtractor;
import net.sourceforge.vrapper.eclipse.platform.EclipseBufferAndTabService;
import net.sourceforge.vrapper.eclipse.utils.Utils;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Listener which adds an {@link InputInterceptor} from the underlying factory
 * to editors.
 * 
 * @author Matthias Radig
 */
public class InputInterceptorManager implements IPartListener2, IPageChangedListener, BufferManager {

    public static final InputInterceptorManager INSTANCE = new InputInterceptorManager(
            VimInputInterceptorFactory.INSTANCE);
    private static final Method METHOD_GET_PAGE_COUNT = getMultiPartEditorMethod("getPageCount");
    private static final Method METHOD_GET_EDITOR = getMultiPartEditorMethod(
            "getEditor", Integer.TYPE);

    private final InputInterceptorFactory factory;
    private Map<IWorkbenchWindow, EclipseBufferAndTabService> bufferAndTabServices;
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;
    private boolean activationListenerEnabled = true;

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

    protected InputInterceptorManager(InputInterceptorFactory factory) {
        this.factory = factory;
        this.bufferAndTabServices = new WeakHashMap<IWorkbenchWindow, EclipseBufferAndTabService>();
        this.interceptors = new WeakHashMap<IWorkbenchPart, InputInterceptor>();
    }

    public EclipseBufferAndTabService ensureBufferService(IEditorPart editor) {
        IWorkbenchWindow window = editor.getEditorSite().getWorkbenchWindow();
        EclipseBufferAndTabService batservice;
        if (bufferAndTabServices.containsKey(window)) {
            batservice = bufferAndTabServices.get(window);
        } else {
            batservice = new EclipseBufferAndTabService(window, this);
            bufferAndTabServices.put(window, batservice);
        }
        return batservice;
    }

    public void interceptWorkbenchPart(IWorkbenchPart part, NestedEditorPartInfo nestingInfo) {
        if (part == null) {
            return;
        }
        if (nestingInfo == null && part instanceof IEditorPart) {
            nestingInfo = new NestedEditorPartInfo((IEditorPart) part);
        } else if (part instanceof IEditorPart) {
            nestingInfo.addChildEditor((IEditorPart) part);
        }
        if (part instanceof IEditorPart) {
            registerEditorPart(nestingInfo, (IEditorPart) part, false);
        }
        if (part instanceof AbstractTextEditor) {
            AbstractTextEditor editor = (AbstractTextEditor) part;
            interceptAbstractTextEditor(editor);
        } else if (part instanceof MultiPageEditorPart) {
            try {
                MultiPageEditorPart mPart = (MultiPageEditorPart) part;
                int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
                for (int i = 0; i < pageCount; i++) {
                    IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                    if (nestingInfo.containedInTree(subPart)) {
                        continue;
                    }
                    if (subPart != null) {
                        interceptWorkbenchPart(subPart, nestingInfo);
                    }
                }
            } catch (Exception exception) {
                VrapperLog.error("Exception during opening of MultiPageEditorPart",
                        exception);
            }
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                if (nestingInfo.containedInTree(subPart)) {
                    continue;
                }
                interceptWorkbenchPart(subPart, nestingInfo);
            }
        } else {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IConfigurationElement[] configurationElements = registry
                    .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.extractor");
            for (IConfigurationElement element: configurationElements) {
                EditorExtractor extractor = (EditorExtractor) Utils
                        .createGizmoForElementConditionally(
                                part, "part-must-subclass",
                                element, "extractor-class");
                if (extractor != null) {
                    for (AbstractTextEditor ate: extractor.extractATEs(part)) {
                        interceptAbstractTextEditor(ate);
                        registerEditorPart(nestingInfo, ate, false);
                    }
                }
            }
        }
    }

    private void interceptAbstractTextEditor(AbstractTextEditor editor) {
        if (interceptors.containsKey(editor)) {
            return;
        }
        try {
            Method me = AbstractTextEditor.class
                    .getDeclaredMethod("getSourceViewer");
            me.setAccessible(true);
            Object viewer = me.invoke(editor);
            if (viewer != null) {
                // test for needed interfaces
                ITextViewer textViewer = (ITextViewer) viewer;
                ITextViewerExtension textViewerExt = (ITextViewerExtension) viewer;
                EclipseBufferAndTabService batService = ensureBufferService(editor);
                InputInterceptor interceptor = factory.createInterceptor(editor, textViewer, batService);
                CaretPositionHandler caretPositionHandler = interceptor.getCaretPositionHandler();
                CaretPositionUndoHandler caretPositionUndoHandler = interceptor.getCaretPositionUndoHandler();
                SelectionVisualHandler visualHandler = interceptor.getSelectionVisualHandler();
                interceptor.getEditorAdaptor().addVrapperEventListener(interceptor.getCaretPositionUndoHandler());

                textViewerExt.prependVerifyKeyListener(interceptor);
                textViewer.getTextWidget().addMouseListener(caretPositionHandler);
                textViewer.getTextWidget().addCaretListener(caretPositionHandler);
                textViewer.getSelectionProvider().addSelectionChangedListener(visualHandler);
                IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
                operationHistory.addOperationHistoryListener(caretPositionUndoHandler);
                interceptors.put(editor, interceptor);
                VrapperPlugin.getDefault().registerEditor(editor, interceptor.getEditorAdaptor());
            }
        } catch (Exception exception) {
            VrapperLog.error("Exception when intercepting AbstractTextEditor",
                    exception);
        }
    }

    public void partClosed(IWorkbenchPart part, NestedEditorPartInfo nestingInfo) {
        InputInterceptor interceptor = interceptors.remove(part);
        if (nestingInfo == null && part instanceof IEditorPart) {
            nestingInfo = new NestedEditorPartInfo((IEditorPart) part);
        } else if (part instanceof IEditorPart) {
            nestingInfo.addChildEditor((IEditorPart) part);
        }
        // remove the listener in case the editor gets cached
        if (interceptor != null) {
            try {
                Method me = AbstractTextEditor.class
                        .getDeclaredMethod("getSourceViewer");
                me.setAccessible(true);
                Object viewer = me.invoke(part);
                // test for needed interfaces
                ITextViewer textViewer = (ITextViewer) viewer;
                ITextViewerExtension textViewerExt = (ITextViewerExtension) viewer;
                CaretPositionHandler caretPositionHandler = interceptor.getCaretPositionHandler();
                CaretPositionUndoHandler caretPositionUndoHandler = interceptor.getCaretPositionUndoHandler();
                SelectionVisualHandler visualHandler = interceptor.getSelectionVisualHandler();
                textViewerExt.removeVerifyKeyListener(interceptor);
                textViewer.getTextWidget().removeCaretListener(caretPositionHandler);
                textViewer.getTextWidget().removeMouseListener(caretPositionHandler);
                textViewer.getSelectionProvider().removeSelectionChangedListener(visualHandler);
                IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
                operationHistory.removeOperationHistoryListener(caretPositionUndoHandler);
            } catch (Exception exception) {
                VrapperLog.error("Exception during closing IWorkbenchPart",
                        exception);
            }
        }
        if (part instanceof IEditorPart) {
            VrapperPlugin.getDefault().unregisterEditor((IEditorPart) part);
        }
        if (part instanceof MultiPageEditorPart) {
            try {
                MultiPageEditorPart mPart = (MultiPageEditorPart) part;
                int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
                for (int i = 0; i < pageCount; i++) {
                    IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                    if (nestingInfo.containedInTree(subPart)) {
                        continue;
                    }
                    partClosed(subPart, nestingInfo);
                }
            } catch (Exception exception) {
                VrapperLog.error("Exception during closing MultiPageEditorPart",
                        exception);
            }
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                if (nestingInfo.containedInTree(subPart)) {
                    continue;
                }
                partClosed(subPart, nestingInfo);
            }
        }
    }

    public void partActivated(IWorkbenchPart part, NestedEditorPartInfo nestingInfo) {
        InputInterceptor input = interceptors.get(part);
        if (nestingInfo == null && part instanceof IEditorPart) {
            nestingInfo = new NestedEditorPartInfo((IEditorPart) part);
        } else if (part instanceof IEditorPart) {
            nestingInfo.addChildEditor((IEditorPart) part);
        }
        if(input == null) {
            try {
                if (part instanceof MultiPageEditorPart) {
                    MultiPageEditorPart mPart = (MultiPageEditorPart) part;
                    int activePage = mPart.getActivePage();
                    int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
                    for (int i = 0; i < pageCount; i++) {
                        IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                        if (nestingInfo.containedInTree(subPart)) {
                            continue;
                        }
                        partActivated(subPart, nestingInfo);
                    }
                    if (activePage != -1) {
                        IEditorPart curEditor = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, activePage);
                        if (curEditor != null) {
                            ensureBufferService(mPart).setCurrentEditor(nestingInfo, curEditor);
                        }
                    }
                }
                else if (part instanceof MultiEditor) {
                    MultiEditor mEditor = (MultiEditor) part;
                    for (IEditorPart subPart : mEditor.getInnerEditors()) {
                        if (nestingInfo.containedInTree(subPart)) {
                            continue;
                        }
                        if (subPart != null) {
                            partActivated(subPart, nestingInfo);
                        }
                    }
                    IEditorPart curEditor = mEditor.getActiveEditor();
                    if (curEditor != null) {
                        ensureBufferService(mEditor).setCurrentEditor(nestingInfo, curEditor);
                    }
                }
            }
            catch (Exception exception) {
                VrapperLog.error("Exception activating MultiPageEditorPart", exception);
            }
        }
        else {
            //changing tab back to existing editor, should we return to NormalMode?
            EditorAdaptor vim = input.getEditorAdaptor();
            if (VrapperPlugin.isVrapperEnabled()
                    && vim.getConfiguration().get(Options.START_NORMAL_MODE)) {
                vim.setSelection(null);
                vim.changeModeSafely(NormalMode.NAME);
            }
            // Multi-page editors should set their active page at the end, see above.
            if (nestingInfo.getParentEditor().equals(part)) {
                IEditorPart editor = (IEditorPart) part;
                ensureBufferService(editor).setCurrentEditor(nestingInfo, editor);
            }
        }
    }

    private static Method getMultiPartEditorMethod(String name,
            Class<?>... args) {
        try {
            Method m = MultiPageEditorPart.class.getDeclaredMethod(name, args);
            m.setAccessible(true);
            return m;
        } catch (Exception exception) {
            VrapperLog.error("Exception extracting MultiPageEditorPart method",
                    exception);
        }
        return null;
    }

    public Iterable<InputInterceptor> getInterceptors() {
        return interceptors.values();
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        if ( ! activationListenerEnabled) {
            return;
        }
        partActivated(partRef.getPart(true), null);
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        partClosed(partRef.getPart(true), null);
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        interceptWorkbenchPart(partRef.getPart(true), null);
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        final IWorkbenchPart part = partRef.getPart(true);
        // The underlying editor has changed for the part -- reset Vrapper's
        // editor-related references.
        partClosed(part, null);
        interceptWorkbenchPart(part, null);
    }

    @Override
    public void pageChanged(PageChangedEvent event) {
        if ( ! activationListenerEnabled) {
            return;
        }
        if (event.getPageChangeProvider() instanceof IEditorPart
                && event.getSelectedPage() instanceof IEditorPart) {
            IEditorPart parentEditor = (IEditorPart) event.getPageChangeProvider();
            IEditorPart editor = (IEditorPart) event.getSelectedPage();
            NestedEditorPartInfo info = new NestedEditorPartInfo(parentEditor, editor);
            partActivated(editor, info);
            ensureBufferService(editor).setCurrentEditor(info, editor);
        }
    }

    /* Buffer ID managing code */

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
        // Spotted in the wild, some child editors of a multi-page editor don't have an input.
        if (input == null) {
            return;
        }

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
                if (reservedBuffer != null) {
                    info.seenWindows.putAll(reservedBuffer.seenWindows);
                }
            } else {
                // Each child buffer gets its own id.
                bufferId = BUFFER_ID_SEQ.incrementAndGet();
                // Nested editors don't return reliable info, ask parent editor.
                IEditorInput parentInput = nestingInfo.getParentEditor().getEditorInput();
                documentType = nestingInfo.getParentEditor().getEditorSite().getId();
                info = new BufferInfo(bufferId, editorPart, parentInput, documentType, input);
                if (reservedBuffer != null) {
                    info.seenWindows.putAll(reservedBuffer.seenWindows);
                }
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
            bufferInfo.seenWindows.put(editorPart.getEditorSite().getWorkbenchWindow(), null);
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
    
    public void activate(BufferInfo buffer) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (buffer.reference != null) {
            IEditorPart editor = buffer.reference.getEditor(true);
            if (editor == null) {
                throw new VrapperPlatformException("Failed to activate editor for reference "
                        + buffer.reference);
            }
            buffer.reference.getPage().activate(editor);
        } else if (buffer.input != null && buffer.parentInput == null) {
            try {
                page.openEditor(buffer.input, buffer.editorType, true,
                        IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
            } catch (PartInitException e) {
                throw new VrapperPlatformException("Failed to activate editor for input "
                    + buffer.input + ", type " + buffer.editorType, e);
            }
        } else if (buffer.input != null) {
            IEditorPart parentEditor;
            // Disable listener - multi-page editors can start with any page active so triggering
            // partActivated listeners only clobbers the current editor status.
            activationListenerEnabled = false;
            try {
                IEditorReference[] editors = page.findEditors(buffer.parentInput, buffer.editorType,
                        IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
                // Directly activate existing editors as some editor implementations tend to reset
                // the cursor when "re-opened".
                if (editors.length > 0) {
                    parentEditor = editors[0].getEditor(true);
                    page.activate(parentEditor);
                } else {
                    parentEditor = page.openEditor(buffer.parentInput, buffer.editorType, true,
                        IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
                }
            } catch (PartInitException e) {
                throw new VrapperPlatformException("Failed to activate editor for input "
                    + buffer.input + ", type " + buffer.editorType, e);
            } finally {
                activationListenerEnabled = true;
            }
            activateInnerEditor(buffer, parentEditor);
        } else {
            throw new VrapperPlatformException("Found bufferinfo object with no editor info!"
                    + " This is most likely a bug.");
        }
    }

    protected void activateInnerEditor(BufferInfo buffer, IEditorPart parentEditor) {
        if (parentEditor instanceof MultiPageEditorPart) {
            MultiPageEditorPart multiPage = (MultiPageEditorPart) parentEditor;
            IEditorPart[] foundEditors = multiPage.findEditors(buffer.input);
            if (foundEditors.length < 1) {
                throw new VrapperPlatformException("Failed to find inner editor for "
                        + buffer.input + " in parent editor " + parentEditor);
            }
            IEditorPart editor = foundEditors[0];
            int activePage = multiPage.getActivePage();
            boolean activated = false;
            if (activePage != -1) {
                IEditorPart innerEditor;
                try {
                    innerEditor = (IEditorPart) METHOD_GET_EDITOR.invoke(multiPage, activePage);
                } catch (Exception e) {
                    throw new VrapperPlatformException("Failed to get active page of " + multiPage, e);
                }
                // The current page is matching our target page. Don't activate it again so that
                // the editor won't reset cursor position (as seen in the XML editors)
                if (innerEditor != null && innerEditor.getEditorInput().equals(buffer.input)) {
                    NestedEditorPartInfo info = new NestedEditorPartInfo(parentEditor, innerEditor);
                    // Update active editor info because no listener was called.
                    ensureBufferService(multiPage).setCurrentEditor(info, innerEditor);
                    activated = true;
                }
            }
            if ( ! activated) {
                // Current buffer info will be set through page change listener.
                multiPage.setActiveEditor(editor);
            }
        } else if (parentEditor instanceof MultiEditor) {
            MultiEditor editor = (MultiEditor) parentEditor;
            IEditorPart[] innerEditors = editor.getInnerEditors();
            int i = 0;
            while (i < innerEditors.length
                    && ! buffer.input.equals(innerEditors[i].getEditorInput())) {
                i++;
            }
            if (i < innerEditors.length) {
                IEditorPart innerEditor = innerEditors[i];
                editor.activateEditor(innerEditor);
                // Explicitly set active editor because we don't have a listener here
                NestedEditorPartInfo info = new NestedEditorPartInfo(parentEditor, innerEditor);
                partActivated(innerEditor, info);
                ensureBufferService(editor).setCurrentEditor(info, innerEditor);
            }
        }
    }
}
