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
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
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
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;
    private final Map<IWorkbenchPart, Collection<WeakReference<IWorkbenchPart>>> watchedChildren;

    protected InputInterceptorManager(InputInterceptorFactory factory) {
        this.factory = factory;
        this.interceptors = new WeakHashMap<IWorkbenchPart, InputInterceptor>();
        this.watchedChildren = new WeakHashMap<IWorkbenchPart, Collection<WeakReference<IWorkbenchPart>>>();
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
                interceptWorkbenchPart(subPart);
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
                ITextViewer textViewer = (ITextViewer) viewer;
                ITextViewerExtension textViewerExt = (ITextViewerExtension) viewer;
                InputInterceptor interceptor = factory.createInterceptor(editor, textViewer);
                CaretPositionHandler caretPositionHandler = interceptor.getCaretPositionHandler();

                textViewerExt.prependVerifyKeyListener(interceptor);
                textViewer.getTextWidget().addMouseListener(caretPositionHandler);
                textViewer.getTextWidget().addCaretListener(caretPositionHandler);
                textViewer.getSelectionProvider().addSelectionChangedListener(interceptor);
                interceptors.put(editor, interceptor);
                VrapperPlugin.getDefault().registerEditor(editor, interceptor.getEditorAdaptor());
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
                interceptWorkbenchPart(subPart);
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
                ITextViewer textViewer = (ITextViewer) viewer;
                ITextViewerExtension textViewerExt = (ITextViewerExtension) viewer;
                CaretPositionHandler caretPositionHandler = interceptor.getCaretPositionHandler();
                textViewerExt.removeVerifyKeyListener(interceptor);
                textViewer.getTextWidget().removeCaretListener(caretPositionHandler);
                textViewer.getTextWidget().removeMouseListener(caretPositionHandler);
                textViewer.getSelectionProvider().removeSelectionChangedListener(interceptor);
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
    	InputInterceptor input = interceptors.get(arg0);
    	if(input == null) {
    		try {
    			if (arg0 instanceof MultiPageEditorPart) {
    				MultiPageEditorPart mPart = (MultiPageEditorPart) arg0;
    				int pageCount = ((Integer) METHOD_GET_PAGE_COUNT.invoke(arg0)).intValue();
    				for (int i = 0; i < pageCount; i++) {
    					IEditorPart subPart = (IEditorPart) METHOD_GET_EDITOR.invoke(mPart, i);
    					partActivated(subPart);
    				}
    			}
    			else if (arg0 instanceof MultiEditor) {
    				for (IEditorPart subPart : ((MultiEditor) arg0).getInnerEditors()) {
    					partActivated(subPart);
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
        this.partActivated(partRef.getPart(true));
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        partClosed(partRef.getPart(true));
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        interceptWorkbenchPart(partRef.getPart(true));
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
        partClosed(part);
        interceptWorkbenchPart(part);
    }

}
