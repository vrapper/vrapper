package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
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

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.extractor.EditorExtractor;
import net.sourceforge.vrapper.eclipse.platform.EclipseBufferAndTabService;
import net.sourceforge.vrapper.eclipse.platform.EclipseCursorAndSelection;
import net.sourceforge.vrapper.eclipse.utils.Utils;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.PlatformVrapperLifecycleListener;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.NormalMode;

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

    /** Helper class which initializes the Vrapper machinery for each given editor. */
    private final InputInterceptorFactory factory;

    /**
     * Map of BufferAndTabServices, one per workbench window. This allows to have a buffer list
     * and "current editor" specific to each window.
     */
    private Map<IWorkbenchWindow, EclipseBufferAndTabService> bufferAndTabServices;

    /** Map holding all currently active editors and their associated Vrapper machinery. */
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;

    /**
     * Map holding nested editor info for all activated top-level editors. Stored here so that we
     * don't need to build this every time we come across an editor.
     */
    private final Map<IWorkbenchPart, EditorInfo> toplevelEditorInfo;

    /**
     * Flag which (temporarily) suspends the partActivated method when we know the activated part
     * will immediately change.
     */
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
     * Buffer ids for all files which have been opened once in an active editor.
     * <p>This Map can contain buffer information for files which are no longer open.
     */
    protected WeakHashMap<IEditorInput,BufferInfo> activeBufferIdMapping =
            new WeakHashMap<IEditorInput, BufferInfo>();

    protected List<PlatformVrapperLifecycleListener> lifecycleListeners = Collections.emptyList();

    /** Buffer ID generator. */
    protected final static AtomicInteger BUFFER_ID_SEQ = new AtomicInteger();

    protected InputInterceptorManager(InputInterceptorFactory factory) {
        this.factory = factory;
        this.bufferAndTabServices = new WeakHashMap<IWorkbenchWindow, EclipseBufferAndTabService>();
        this.interceptors = new WeakHashMap<IWorkbenchPart, InputInterceptor>();
        this.toplevelEditorInfo = new WeakHashMap<IWorkbenchPart, EditorInfo>();

        IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        IConfigurationElement[] elements = registry
                .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.lifecyclelistener");
        List<PlatformVrapperLifecycleListener> matched = new ArrayList<PlatformVrapperLifecycleListener>();
        for (final IConfigurationElement element : elements) {
            String listenerClass = element.getAttribute("listener-class");
            try {
                matched.add((PlatformVrapperLifecycleListener)
                        element.createExecutableExtension("listener-class"));
            } catch (final Exception e) {
                VrapperLog.error("error while building vrapper lifecycle listener " + listenerClass, e);
            }
        }
        lifecycleListeners = matched;
    }

    public EclipseBufferAndTabService ensureBufferService(IEditorPart editor) {
        IWorkbenchWindow window = editor.getEditorSite().getWorkbenchWindow();
        EclipseBufferAndTabService batservice = bufferAndTabServices.get(window);
        if (batservice == null) {
            batservice = new EclipseBufferAndTabService(window, this);
            bufferAndTabServices.put(window, batservice);
        }
        return batservice;
    }

    public void interceptWorkbenchPart(EditorInfo nestingInfo, ProcessedInfo processedInfo) {

        registerEditorPart(nestingInfo, false);

        IEditorPart part = nestingInfo.getCurrent();
        if (part instanceof AbstractTextEditor) {
            AbstractTextEditor editor = (AbstractTextEditor) part;
            interceptAbstractTextEditor(editor, nestingInfo);
        } else if (part instanceof MultiPageEditorPart) {
            try {
                MultiPageEditorPart mPart = (MultiPageEditorPart) part;
                int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
                for (int i = 0; i < pageCount; i++) {
                    IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                    if (subPart == null || processedInfo.isProcessed(subPart)) {
                        continue;
                    }
                    if (subPart != null) {
                        interceptWorkbenchPart(nestingInfo.createChildInfo(subPart),
                                processedInfo.markPart(subPart));
                    }
                }
            } catch (Exception exception) {
                VrapperLog.error("Exception during opening of MultiPageEditorPart",
                        exception);
            }
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                if (subPart == null || processedInfo.isProcessed(subPart)) {
                    continue;
                }
                interceptWorkbenchPart(nestingInfo.createChildInfo(subPart),
                        processedInfo.markPart(subPart));
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
                    for (AbstractTextEditor ate: extractor.extractATEs(nestingInfo)) {
                        interceptAbstractTextEditor(ate, nestingInfo);
                        registerEditorPart(nestingInfo, false);
                    }
                }
            }
        }
    }

    private void interceptAbstractTextEditor(AbstractTextEditor editor, EditorInfo partInfo) {
        if (interceptors.containsKey(editor)) {
            return;
        }
        try {
            Method me = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
            me.setAccessible(true);
            Object viewer = me.invoke(editor);
            if (viewer != null) {
                // test for needed interfaces
                ISourceViewer srcViewer = (ISourceViewer) viewer;
                ITextViewerExtension textViewerExt = (ITextViewerExtension) viewer;
                EclipseBufferAndTabService batService = ensureBufferService(editor);
                InputInterceptor interceptor = factory.createInterceptor(editor, srcViewer,
                        partInfo, batService, lifecycleListeners);
                CaretPositionHandler caretPositionHandler = interceptor.getCaretPositionHandler();
                CaretPositionUndoHandler caretPositionUndoHandler = interceptor.getCaretPositionUndoHandler();
                SelectionVisualHandler visualHandler = interceptor.getSelectionVisualHandler();
                EclipseCursorAndSelection selectionSvc = interceptor.getPlatform().getSelectionService();

                textViewerExt.prependVerifyKeyListener(interceptor);
                srcViewer.getTextWidget().addMouseListener(caretPositionHandler);
                srcViewer.getTextWidget().addCaretListener(caretPositionHandler);
                srcViewer.getSelectionProvider().addSelectionChangedListener(visualHandler);
                IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
                operationHistory.addOperationHistoryListener(caretPositionUndoHandler);
                selectionSvc.installHooks();

                for (PlatformVrapperLifecycleListener listener : lifecycleListeners) {
                    try {
                        listener.editorInitialized(interceptor.getEditorAdaptor(),
                                VrapperPlugin.isVrapperEnabled());
                    } catch (Exception e) {
                        String currentFilePath = interceptor.getPlatform().getFileService().getCurrentFilePath();
                        VrapperLog.error("Lifecycle listener " + listener.getClass() + "' threw "
                                + "exception for file '" + currentFilePath + "' when firing "
                                        + "'editorInitialized' method.", e);
                    }
                }
                interceptors.put(editor, interceptor);
            }
        } catch (Exception exception) {
            VrapperLog.error("Exception when intercepting AbstractTextEditor",
                    exception);
        }
    }

    public void partClosed(EditorInfo nestingInfo, ProcessedInfo processedInfo) {

        IEditorPart part = nestingInfo.getCurrent();
        InputInterceptor interceptor = interceptors.remove(part);
        // remove the listener in case the editor gets cached
        if (interceptor != null) {
            for (PlatformVrapperLifecycleListener listener : lifecycleListeners) {
                try {
                    listener.editorClosing(interceptor.getEditorAdaptor(),
                            VrapperPlugin.isVrapperEnabled());
                } catch (Exception e) {
                    String currentFilePath = interceptor.getPlatform().getFileService().getCurrentFilePath();
                    VrapperLog.error("Lifecycle listener " + listener.getClass() + "' threw "
                            + "exception for file '" + currentFilePath + "' when firing "
                            + "editorClosing' method.", e);
                }
            }
            try {
                interceptor.getEditorAdaptor().close();
            } catch (Exception exception) {
                VrapperLog.error("Exception while closing EditorAdaptor.", exception);
            }
            try {
                Method me = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
                me.setAccessible(true);
                Object viewer = me.invoke(part);
                // test for needed interfaces
                ITextViewer textViewer = (ITextViewer) viewer;
                ITextViewerExtension textViewerExt = (ITextViewerExtension) viewer;
                CaretPositionHandler caretPositionHandler = interceptor.getCaretPositionHandler();
                CaretPositionUndoHandler caretPositionUndoHandler = interceptor.getCaretPositionUndoHandler();
                SelectionVisualHandler visualHandler = interceptor.getSelectionVisualHandler();
                EclipseCursorAndSelection selectionSvc = interceptor.getPlatform().getSelectionService();

                textViewerExt.removeVerifyKeyListener(interceptor);
                textViewer.getTextWidget().removeCaretListener(caretPositionHandler);
                textViewer.getTextWidget().removeMouseListener(caretPositionHandler);
                textViewer.getSelectionProvider().removeSelectionChangedListener(visualHandler);
                IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
                operationHistory.removeOperationHistoryListener(caretPositionUndoHandler);
                selectionSvc.uninstallHooks();
            } catch (Exception exception) {
                VrapperLog.error("Exception during closing IWorkbenchPart",
                        exception);
            }
        }
        if (part instanceof MultiPageEditorPart) {
            try {
                MultiPageEditorPart mPart = (MultiPageEditorPart) part;
                int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
                for (int i = 0; i < pageCount; i++) {
                    IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                    if (subPart == null || processedInfo.isProcessed(subPart)) {
                        continue;
                    }
                    // No cleanup needed if this tab page is added later on. Seen in the wild.
                    if (nestingInfo.getChild(subPart) != null) {
                        partClosed(nestingInfo.getChild(subPart), processedInfo.markPart(subPart));
                    }
                }
            } catch (Exception exception) {
                VrapperLog.error("Exception during closing MultiPageEditorPart",
                        exception);
            }
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                if (subPart == null || processedInfo.isProcessed(subPart)) {
                    continue;
                }
                partClosed(nestingInfo.createChildInfo(subPart), processedInfo.markPart(subPart));
            }
        }
    }

    public void partActivated(EditorInfo editorInfo, ProcessedInfo processedInfo) {

        IEditorPart part = editorInfo.getCurrent();
        InputInterceptor input = interceptors.get(part);

        if (input == null) {
            try {
                if (part instanceof MultiPageEditorPart) {
                    MultiPageEditorPart mPart = (MultiPageEditorPart) part;
                    int activePage = mPart.getActivePage();
                    int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part)).intValue();
                    for (int i = 0; i < pageCount; i++) {
                        IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
                        if (subPart == null || processedInfo.isProcessed(subPart)) {
                            continue;
                        }
                        // Nested editor might be added later on, as seen in the wild
                        if (editorInfo.getChild(subPart) == null) {
                            VrapperLog.info("Editor " + editorInfo.getTopLevelEditor()
                                    + " dynamically added page " + subPart);
                            interceptWorkbenchPart(editorInfo.createChildInfo(subPart),
                                    processedInfo.markPart(subPart));
                        } else {
                            partActivated(editorInfo.getChild(subPart), processedInfo.markPart(subPart));
                        }
                    }
                    if (activePage != -1) {
                        IEditorPart curEditor = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, activePage);
                        if (curEditor != null) {
                            ensureBufferService(mPart).setCurrentEditor(editorInfo.getChild(curEditor));
                        }
                    }
                }
                else if (part instanceof MultiEditor) {
                    MultiEditor mEditor = (MultiEditor) part;
                    for (IEditorPart subPart : mEditor.getInnerEditors()) {
                        if (subPart == null || processedInfo.isProcessed(subPart)) {
                            continue;
                        }
                        // Nested editor might be added later on, as seen in the wild
                        if (editorInfo.getChild(subPart) == null) {
                            VrapperLog.info("Editor " + editorInfo.getTopLevelEditor()
                                    + " dynamically added page " + subPart);
                            interceptWorkbenchPart(editorInfo.createChildInfo(subPart),
                                    processedInfo.markPart(subPart));
                        } else {
                            partActivated(editorInfo.getChild(subPart), processedInfo.markPart(subPart));
                        }
                    }
                    IEditorPart curEditor = mEditor.getActiveEditor();
                    if (curEditor != null) {
                        ensureBufferService(mEditor).setCurrentEditor(editorInfo.getChild(curEditor));
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
                // Make sure caret is placed within line, reset caret shape for conflicted selection
                if (vim.getCurrentMode() instanceof NormalMode) {
                    ((NormalMode)vim.getCurrentMode()).placeCursor(StickyColumnPolicy.NEVER);
                }
            }
            // Simple editors are marked as active here. Multi-page editors should set their
            // current editor once after calling recursively (see above).
            if (editorInfo.isSimpleEditor()) {
                IEditorPart editor = (IEditorPart) part;
                ensureBufferService(editor).setCurrentEditor(editorInfo);
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

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        if ( ! activationListenerEnabled) {
            return;
        }
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof IEditorPart) {
            IEditorPart editor = (IEditorPart) part;
            EditorInfo editorInfo = toplevelEditorInfo.get(editor);
            // While *very rare*, some editors manage to sneak up on Vrapper by skipping partOpened.
            if (editorInfo == null) {
                editorInfo = new EditorInfo(editor);
                toplevelEditorInfo.put(editor, editorInfo);
                interceptWorkbenchPart(editorInfo, new ProcessedInfo(editor));
            } else {
                partActivated(editorInfo, new ProcessedInfo(editor));
            }
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof IEditorPart) {
            IEditorPart editor = (IEditorPart) part;
            EditorInfo nestedInfo = toplevelEditorInfo.get(editor);
            if (nestedInfo == null) {
                nestedInfo = new EditorInfo(editor);
            }
            partClosed(nestedInfo, new ProcessedInfo(editor));
            toplevelEditorInfo.remove(editor);
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part instanceof IEditorPart) {
            IEditorPart editor = (IEditorPart) part;
            EditorInfo nestedInfo = new EditorInfo(editor);
            toplevelEditorInfo.put(editor, nestedInfo);
            interceptWorkbenchPart(nestedInfo, new ProcessedInfo(editor));
        }
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
        if (part instanceof IEditorPart) {
            IEditorPart editor = (IEditorPart) part;
            EditorInfo editorInfo = toplevelEditorInfo.get(editor);
            if (editorInfo != null) {
                partClosed(editorInfo, new ProcessedInfo(editor));
                toplevelEditorInfo.remove(editor);
            }
            // Reset nested structures, changing the input might have shook things around.
            editorInfo = new EditorInfo(editor);
            toplevelEditorInfo.put(editor, editorInfo);
            interceptWorkbenchPart(editorInfo, new ProcessedInfo(editor));
        }
    }

    @Override
    public void pageChanged(PageChangedEvent event) {
        if ( ! activationListenerEnabled) {
            return;
        }
        if (event.getPageChangeProvider() instanceof IEditorPart
                && event.getSelectedPage() instanceof IEditorPart) {
            IEditorPart toplevelEditor = (IEditorPart) event.getPageChangeProvider();
            IEditorPart editor = (IEditorPart) event.getSelectedPage();
            InputInterceptor interceptor = interceptors.get(editor);

            // This can happen for some rare, dynamic editors which replace editor pages later on.
            // They tend to do this upon changing pages in which case they're called before.
            if (interceptor == null) {
                EditorInfo topPageEditorInfo = toplevelEditorInfo.get(toplevelEditor);
                if (topPageEditorInfo != null && editor instanceof AbstractTextEditor) {
                    AbstractTextEditor abstractTextEditor = (AbstractTextEditor) editor;
                    EditorInfo childInfo = topPageEditorInfo.createChildInfo(editor);

                    // Calling partActivated below will update the 'lastSeen' info, pass false.
                    registerEditorPart(childInfo, false);
                    interceptAbstractTextEditor(abstractTextEditor, childInfo);
                    interceptor = interceptors.get(editor);
                }
            }

            if (interceptor != null) {
                EditorInfo info = interceptor.getEditorInfo();
                partActivated(info, new ProcessedInfo(editor));
                ensureBufferService(editor).setCurrentEditor(info);
            }
        }
    }

    /* Buffer ID managing code */

    public void registerEditorRef(IEditorReference ref) {
        if ( ! reservedBufferIdMapping.containsKey(ref)) {
            int bufferId = BUFFER_ID_SEQ.incrementAndGet();
            reservedBufferIdMapping.put(ref, new BufferInfo(bufferId, ref, ref.getId()));
        }
    }

    public void registerEditorPart(EditorInfo editorInfo, boolean updateLastSeen) {
        IEditorPart editorPart = editorInfo.getCurrent();
        IEditorInput input = editorPart.getEditorInput();

        // Spotted in the wild, some child editors of a multi-page editor don't have an input.
        if (input == null) {
            return;
        }

        IWorkbenchPage page = editorPart.getEditorSite().getPage();
        // Remove any lingering references in case input was opened in two different editors.
        BufferInfo reservedBuffer = reservedBufferIdMapping.remove(
                page.getReference(editorInfo.getTopLevelEditor())); // TODO cache reference

        if ( ! activeBufferIdMapping.containsKey(input)) {
            IEditorInput parentInput = null; // Not needed in case of simple editor
            int id;
            if (editorInfo.isSimpleEditor()) {
                // Always use existing id if present.
                if (reservedBuffer == null) {
                    id = BUFFER_ID_SEQ.incrementAndGet();
                } else {
                    id = reservedBuffer.bufferId;
                }
            } else {
                // Each child buffer gets its own id.
                id = BUFFER_ID_SEQ.incrementAndGet();
                // Nested editors don't return reliable info, ask parent editor.
                parentInput = editorInfo.getTopLevelEditor().getEditorInput();
            }
            String parentType = editorInfo.getTopLevelEditor().getEditorSite().getId();
            BufferInfo bufferInfo = new BufferInfo(id, editorPart, parentInput, parentType, input);
            if (reservedBuffer != null) {
                bufferInfo.seenWindows.putAll(reservedBuffer.seenWindows);
            }
            activeBufferIdMapping.put(input, bufferInfo);
        } else {
            // Verify if editorinput is still being edited in the same editor. It's possible that
            // a file is reopened in another editor, e.g. through "Open with" or a multipage editor.
            BufferInfo bufferInfo = activeBufferIdMapping.get(input);
            IEditorPart lastSeenEditor = bufferInfo.lastSeenEditor.get();

            if ( ! editorPart.equals(lastSeenEditor) && updateLastSeen) {
                if (editorInfo.isSimpleEditor()) {
                    bufferInfo.parentInput = null;
                } else {
                    bufferInfo.parentInput = editorInfo.getParentInfo().getCurrent().getEditorInput();
                }
                bufferInfo.editorType = editorInfo.getTopLevelEditor().getEditorSite().getId();
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
            // Open the reference in its own page, duplicating an unloaded reference is risky.
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
            EditorInfo parentEditorInfo = toplevelEditorInfo.get(parentEditor);
            activateInnerEditor(buffer, parentEditorInfo);
        } else {
            throw new VrapperPlatformException("Found bufferinfo object with no editor input info!"
                    + " This is most likely a bug.");
        }
    }

    protected void activateInnerEditor(BufferInfo buffer, EditorInfo parentEditorInfo) {
        IEditorPart parentEditor = parentEditorInfo.getCurrent();
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
            // Check if the current page is matching our target page. If so, don't activate it again
            // so that the editor won't reset cursor position (as seen in the XML editors)
            if (activePage != -1) {
                IEditorPart innerEditor;
                try {
                    innerEditor = (IEditorPart) METHOD_GET_EDITOR.invoke(multiPage, activePage);
                } catch (Exception e) {
                    throw new VrapperPlatformException("Failed to get active page of " + multiPage, e);
                }
                if (innerEditor != null && innerEditor.getEditorInput().equals(buffer.input)) {
                    EditorInfo innerInfo = parentEditorInfo.getChild(innerEditor);
                    if (innerInfo == null) {
                        throw new VrapperPlatformException("Unknown child editor " + innerEditor);
                    }
                    // Update active editor info because no listener was called.
                    ensureBufferService(multiPage).setCurrentEditor(innerInfo);
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
                EditorInfo innerInfo = parentEditorInfo.getChild(innerEditor);
                if (innerInfo == null) {
                    throw new VrapperPlatformException("Unknown child editor " + innerEditor);
                }
                partActivated(innerInfo, new ProcessedInfo(innerEditor));
                ensureBufferService(editor).setCurrentEditor(innerInfo);
            }
        }
    }

    @Override
    public Map<IWorkbenchPart, InputInterceptor> getInterceptors() {
        return Collections.unmodifiableMap(interceptors);
    }

    public InputInterceptor findActiveInterceptor(IWorkbenchPart toplevelEditor)
            throws UnknownEditorException, VrapperPlatformException {
        EditorInfo editorInfo = toplevelEditorInfo.get(toplevelEditor);
        if (editorInfo == null) {
            throw new UnknownEditorException("No editor info found for editor " + toplevelEditor
                    + ". This might not be a top-level editor.");
        }
        return findActiveInterceptor(editorInfo, new ProcessedInfo(toplevelEditor));
    }
    
    protected InputInterceptor findActiveInterceptor(EditorInfo editorInfo,
            ProcessedInfo processedInfo) throws UnknownEditorException, VrapperPlatformException {
        IWorkbenchPart part = editorInfo.getCurrent();
        InputInterceptor result;
        if (part instanceof MultiPageEditorPart) {
            MultiPageEditorPart mPart = (MultiPageEditorPart) part;
            int activePage = mPart.getActivePage();
            try {
                IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, activePage);
                result = interceptors.get(subPart);
            } catch (Exception e) {
                throw new VrapperPlatformException("Failed to get active input interceptor for "
                        + editorInfo.getTopLevelEditor(), e);
            }
        } else if (part instanceof MultiEditor) {
            MultiEditor multiEditor = (MultiEditor) part;
            result = interceptors.get(multiEditor.getActiveEditor());
        } else if (part instanceof AbstractTextEditor) {
            result = interceptors.get(part);
        } else {
            throw new UnknownEditorException("Cannot find active input interceptor for editor "
                    + editorInfo.getTopLevelEditor() + ". Unknown sub-editor type " + part);
        }
        if (result == null) {
            throw new UnknownEditorException("Cannot find active input interceptor for editor "
                    + editorInfo.getTopLevelEditor() + ". Possibly on an unsupported tab.");
        }
        return result;
    }
    
    @Override
    public void activate(InputInterceptor interceptor) {
        EditorInfo editorInfo = interceptor.getEditorInfo();

        // We could do a lookup in the mapped buffer list, but it's possibly wrong if there are
        // duplicate editors. Also, activate(BufferInfo) doesn't care about the id.
        BufferInfo dummyBuffer;
        IEditorInput parentInput = null;
        if ( ! editorInfo.isSimpleEditor()) {
            parentInput = editorInfo.getTopLevelEditor().getEditorInput();
        }
        dummyBuffer = new BufferInfo(-1, editorInfo.getCurrent(), parentInput,
                editorInfo.getTopLevelEditor().getEditorSite().getId(),
                editorInfo.getCurrent().getEditorInput());
        activate(dummyBuffer);
    }
}
