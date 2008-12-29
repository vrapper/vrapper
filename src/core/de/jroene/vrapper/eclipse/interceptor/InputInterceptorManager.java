package de.jroene.vrapper.eclipse.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
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

    public InputInterceptorManager(InputInterceptorFactory factory, IWorkbenchWindow window) {
        super();
        this.factory = factory;
        this.window = window;
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
                textViewer.appendVerifyKeyListener(factory.createInterceptor(window, editor, (ITextViewer)textViewer));
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
        }
    }

    public void partClosed(IWorkbenchPart arg0) {
        // TODO Auto-generated method stub

    }

    protected void clean() {

    }

    public void partActivated(IWorkbenchPart arg0) {
        // TODO Auto-generated method stub

    }

    public void partBroughtToTop(IWorkbenchPart arg0) {
    }
}
