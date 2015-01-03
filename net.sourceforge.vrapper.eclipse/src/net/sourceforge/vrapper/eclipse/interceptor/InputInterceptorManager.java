package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.extractor.EditorExtractor;
import net.sourceforge.vrapper.eclipse.platform.EclipseBufferAndTabService;
import net.sourceforge.vrapper.eclipse.utils.Utils;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
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
public class InputInterceptorManager implements IPartListener2 {

    public static final InputInterceptorManager INSTANCE = new InputInterceptorManager(
            VimInputInterceptorFactory.INSTANCE);
    private static final Method METHOD_GET_PAGE_COUNT = getMultiPartEditorMethod("getPageCount");
    private static final Method METHOD_GET_EDITOR = getMultiPartEditorMethod(
            "getEditor", Integer.TYPE);

    private final InputInterceptorFactory factory;
    private EclipseBufferAndTabService bufferAndTabService;
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;

    protected InputInterceptorManager(InputInterceptorFactory factory) {
        this.factory = factory;
        this.bufferAndTabService = new EclipseBufferAndTabService(BufferManager.INSTANCE);
        this.interceptors = new WeakHashMap<IWorkbenchPart, InputInterceptor>();
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
            BufferManager.INSTANCE.registerEditorPart(nestingInfo, (IEditorPart) part, false);
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
                        BufferManager.INSTANCE.registerEditorPart(nestingInfo, ate, false);
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
                InputInterceptor interceptor = factory.createInterceptor(editor, textViewer, bufferAndTabService);
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
                        IEditorPart activeEditor = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, activePage);
                        if (activeEditor != null) {
                            bufferAndTabService.setCurrentEditor(nestingInfo, activeEditor);
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
                    if (mEditor.getActiveEditor() != null) {
                        bufferAndTabService.setCurrentEditor(nestingInfo, mEditor.getActiveEditor());
                    }
                }
            }
            catch (Exception exception) {
                VrapperLog.error("Exception activating MultiPageEditorPart", exception);
            }
        }
        else {
            //changing tab back to existing editor, should we return to NormalMode?
            EditorAdaptor editor = input.getEditorAdaptor();
            if(editor.getConfiguration().get(Options.START_NORMAL_MODE)) {
                editor.setSelection(null);
                editor.changeModeSafely(NormalMode.NAME);
            }
            // Multi-page editors should set their active page at the end, see above.
            if (nestingInfo.getParentEditor().equals(part)) {
                bufferAndTabService.setCurrentEditor(nestingInfo, (IEditorPart) part);
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

}
