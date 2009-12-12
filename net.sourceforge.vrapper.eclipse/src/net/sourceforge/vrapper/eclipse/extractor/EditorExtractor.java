package net.sourceforge.vrapper.eclipse.extractor;

import java.util.Collection;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public interface EditorExtractor {
    /**
     * @param part {@link AbstractTextEditor} to extract {@link AbstractTextEditor}s from
     * @return {@link Collection} of {@link AbstractTextEditor}s that may be empty. <code>null</code> return value is not allowed.
     */
    Collection<AbstractTextEditor> extractATEs(IWorkbenchPart part);
}
