package de.jroene.vrapper.eclipse.interceptor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * An abstract factory for {@link InputInterceptor}s.
 *
 * @author Matthias Radig
 */
public interface InputInterceptorFactory {

    InputInterceptor createInterceptor(IWorkbenchWindow window, AbstractTextEditor part, ITextViewer textViewer);
}
