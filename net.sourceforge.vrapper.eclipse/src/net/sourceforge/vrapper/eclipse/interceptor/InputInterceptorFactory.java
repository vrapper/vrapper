package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * An abstract factory for {@link InputInterceptor}s.
 *
 * @author Matthias Radig
 */
public interface InputInterceptorFactory {

    InputInterceptor createInterceptor(AbstractTextEditor part, ITextViewer textViewer);
    InputInterceptor createInterceptor(EditorAdaptor editorAdaptor);
}
