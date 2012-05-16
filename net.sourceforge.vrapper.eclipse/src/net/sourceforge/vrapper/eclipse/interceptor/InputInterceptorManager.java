package net.sourceforge.vrapper.eclipse.interceptor;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.extractor.EditorExtractor;
import net.sourceforge.vrapper.eclipse.utils.Utils;
import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Listener which adds an {@link InputInterceptor} from the underlying factory
 * to editors.
 * 
 * @author Matthias Radig
 */
public class InputInterceptorManager implements IPartListener {

    public static final InputInterceptorManager INSTANCE = new InputInterceptorManager(
            VimInputInterceptorFactory.INSTANCE);
    private static final Method METHOD_GET_PAGE_COUNT = getMultiPartEditorMethod("getPageCount");
    private static final Method METHOD_GET_EDITOR = getMultiPartEditorMethod(
            "getEditor", Integer.TYPE);

    private final InputInterceptorFactory factory;
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;
    private final Map<IWorkbenchPart, Collection<WeakReference<IWorkbenchPart>>> watchedChildren;

    protected InputInterceptorManager(InputInterceptorFactory factory) {
        this.factory = factory;
        this.interceptors = new WeakHashMap<IWorkbenchPart, InputInterceptor>();
        this.watchedChildren = new WeakHashMap<IWorkbenchPart, Collection<WeakReference<IWorkbenchPart>>>();
    }

    public void partDeactivated(IWorkbenchPart arg0) {
    }

    public void partOpened(IWorkbenchPart part) {
        interceptWorkbenchPart(part);
    }

    public void interceptWorkbenchPart(IWorkbenchPart part) {
        if (part == null) {
            //VrapperLog.error("WTF: null part?!?");
            return;
        }
//        VrapperLog.info(String.format("intercepting %s (%s)", part.getTitle(), part.getClass().getName()));
        if (part instanceof AbstractTextEditor) {
            AbstractTextEditor editor = (AbstractTextEditor) part;
            interceptAbstractTextEditor(editor);
        } else if (part instanceof MultiPageEditorPart) {
            multiPartOpened(part);
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                partOpened(subPart);
            }
        } else {
//            VrapperLog.info("other kind of part opened, trying extensions");
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IConfigurationElement[] configurationElements = registry
                    .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.extractor");
            for (IConfigurationElement element: configurationElements) {
//                VrapperLog.info("trying to use " + element.getAttribute("name"));
                EditorExtractor extractor = (EditorExtractor) Utils
                        .createGizmoForElementConditionally(
                                part, "part-must-subclass",
                                element, "extractor-class");
                if (extractor != null) {
//                    VrapperLog.info("actually using it");
                    for (AbstractTextEditor ate: extractor.extractATEs(part))
                        interceptAbstractTextEditor(ate);
                }
            }
        }
    }

    private void interceptAbstractTextEditor(AbstractTextEditor editor) {
        try {
            Method me = AbstractTextEditor.class
                    .getDeclaredMethod("getSourceViewer");
            me.setAccessible(true);
            Object viewer = me.invoke(editor);
            if (viewer != null) {
                // test for needed interfaces
                ITextViewerExtension textViewer = (ITextViewerExtension) viewer;
                InputInterceptor interceptor = factory.createInterceptor(
                        editor, (ITextViewer) textViewer);
                textViewer.prependVerifyKeyListener(interceptor);
				((ITextViewer) textViewer).getSelectionProvider()
						.addSelectionChangedListener(interceptor);
                interceptors.put(editor, interceptor);
                VrapperPlugin.getDefault().registerEditor(editor);
            }
        } catch (Exception exception) {
            VrapperLog.error("Exception when intercepting AbstractTextEditor",
                    exception);
        }
    }

    private void multiPartOpened(IWorkbenchPart part) {
        try {
            MultiPageEditorPart mPart = (MultiPageEditorPart) part;
            int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part))
                    .intValue();
            for (int i = 0; i < pageCount; i++) {
                IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(
                        mPart, i);
                partOpened(subPart);
            }
        } catch (Exception exception) {
            VrapperLog.error("Exception during opening of MultiPageEditorPart",
                    exception);
        }
    }

    public void partClosed(IWorkbenchPart part) {
        InputInterceptor interceptor = interceptors.remove(part);
        // remove the listener in case the editor gets cached
        if (interceptor != null) {
            try {
                Method me = AbstractTextEditor.class
                        .getDeclaredMethod("getSourceViewer");
                me.setAccessible(true);
                Object viewer = me.invoke(part);
                // test for needed interfaces
                ITextViewerExtension textViewer = (ITextViewerExtension) viewer;
                textViewer.removeVerifyKeyListener(interceptor);
				((ITextViewer) viewer).getSelectionProvider()
						.removeSelectionChangedListener(interceptor);
            } catch (Exception exception) {
                VrapperLog.error("Exception during closing IWorkbenchPart",
                        exception);
            }
        }
        if (part instanceof IEditorPart) {
            VrapperPlugin.getDefault().unregisterEditor((IEditorPart) part);
        }
        if (part instanceof MultiPageEditorPart) {
            multiPartClosed(part);
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                partClosed(subPart);
            }
        }
        if (watchedChildren.containsKey(part)) {
            for (WeakReference<IWorkbenchPart> ref : watchedChildren.get(part)) {
                IWorkbenchPart child = ref.get();
                if (child != null && child != part)
                    partClosed(child);
            }
        }
    }

    private void multiPartClosed(IWorkbenchPart part) {
        try {
            MultiPageEditorPart mPart = (MultiPageEditorPart) part;
            int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(part))
                    .intValue();
            for (int i = 0; i < pageCount; i++) {
                IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(
                        mPart, i);
                partClosed(subPart);
            }
        } catch (Exception exception) {
            VrapperLog.error("Exception during closing MultiPageEditorPart",
                    exception);
        }
    }

    public void partActivated(IWorkbenchPart arg0) {
    }

    public void partBroughtToTop(IWorkbenchPart arg0) {
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

}
