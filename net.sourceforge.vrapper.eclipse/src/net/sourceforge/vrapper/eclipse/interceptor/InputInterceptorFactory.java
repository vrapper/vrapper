package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * An abstract factory for {@link InputInterceptor}s.
 *
 * @author Matthias Radig
 */
public interface InputInterceptorFactory {

    InputInterceptor createInterceptor(AbstractTextEditor part, ISourceViewer textViewer,
            EditorInfo partInfo, BufferAndTabService bufferAndTabService);
    InputInterceptor createInterceptor(EditorAdaptor editorAdaptor);
}
