package net.sourceforge.vrapper.eclipse.extractor;

import java.util.Collection;

import net.sourceforge.vrapper.eclipse.interceptor.NestedEditorPartInfo;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public interface EditorExtractor {
    /**
     * @param nestedEditorPartInfo {@link NestedEditorPartInfo} holding information about the
     *      current editor and its parents. The implementation is supposed to add child IEditor
     *      parts to ease later retrieval.
     * @return {@link Collection} of {@link AbstractTextEditor}s that may be empty. <code>null</code> return value is not allowed.
     */
    Collection<AbstractTextEditor> extractATEs(NestedEditorPartInfo nestedEditorPartInfo);
}
