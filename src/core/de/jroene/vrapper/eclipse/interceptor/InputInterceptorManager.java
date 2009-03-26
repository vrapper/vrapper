package de.jroene.vrapper.eclipse.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Listener which adds an {@link InputInterceptor} from the underlying factory
 * to editors which are opened.
 *
 * @author Matthias Radig
 */
public class InputInterceptorManager implements IPartListener {

    private final InputInterceptorFactory factory;
    private final IWorkbenchWindow window;
    private final Map<IWorkbenchPart, InputInterceptor> interceptors;

    public InputInterceptorManager(InputInterceptorFactory factory, IWorkbenchWindow window) {
        super();
        this.factory = factory;
        this.window = window;
        this.interceptors = new HashMap<IWorkbenchPart, InputInterceptor>();
    }

    public void partDeactivated(IWorkbenchPart arg0) {
    }

    public void partOpened(IWorkbenchPart part) {
        if (part instanceof AbstractTextEditor) {
            AbstractTextEditor editor = (AbstractTextEditor) part;
            try {
                Method me = AbstractTextEditor.class
                .getDeclaredMethod("getSourceViewer");
                me.setAccessible(true);
                Object viewer = me.invoke(editor);
                // test for needed interfaces
                ITextViewerExtension textViewer = (ITextViewerExtension) viewer;
                InputInterceptor interceptor = factory.createInterceptor(window, editor, (ITextViewer)textViewer);
                textViewer.prependVerifyKeyListener(interceptor);
                interceptors.put(part, interceptor);
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        } else if (part instanceof MultiPageEditorPart) {
            multiPartOpened(part);
        } else if (part instanceof MultiEditor) {
            for (IEditorPart subPart : ((MultiEditor) part).getInnerEditors()) {
                partOpened(subPart);
            }
        }
    }

    private void multiPartOpened(IWorkbenchPart part) {
        try {
            Method getPageCount = MultiPageEditorPart.class.getDeclaredMethod("getPageCount");
            Method getEditor = MultiPageEditorPart.class.getDeclaredMethod("getEditor", Integer.TYPE);
            getPageCount.setAccessible(true);
            getEditor.setAccessible(true);
            MultiPageEditorPart mPart = (MultiPageEditorPart) part;
            int pageCount = ((Integer) getPageCount.invoke(part)).intValue();
            for (int i = 0; i < pageCount; i++) {
                IEditorPart subPart = (IEditorPart) getEditor.invoke(mPart, i);
                partOpened(subPart);
            }
        } catch (NoSuchMethodException e) {
            // TODO: handle exception
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void partClosed(IWorkbenchPart arg0) {
        interceptors.remove(arg0);
    }

    protected void clean() {

    }

    public void partActivated(IWorkbenchPart arg0) {
        if (interceptors.containsKey(arg0)) {
            interceptors.get(arg0).partActivated(arg0);
        }
    }

    public void partBroughtToTop(IWorkbenchPart arg0) {
    }
}
