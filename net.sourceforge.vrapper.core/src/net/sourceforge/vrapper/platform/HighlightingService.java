package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.TextRange;

/**
 * Text highlighting service using plugin-provided (plugin.xml) Eclipse annotations.
 */
public interface HighlightingService {

    /**
     * Highlights given region using Eclipse annotation type.
     * @param type Eclipse annotation type.
     * @param name highlighting name.
     * @param region range of text to highlight.
     * @return annotation handle or @a null if there was an error.
     */
    Object highlightRegion(final String type, final String name, final TextRange region);

    /**
     * Highlights a region of @a length model characters starting from @a offset
     * using Eclipse annotation type.
     * @param type Eclipse annotation type.
     * @param name highlighting name.
     * @param offset model offset of the first character to highlight.
     * @param length region length
     * @return annotation handle or @a null if there was an error.
     */
    Object highlightRegion(final String type, final String name,
            final int offset, final int length);

    /**
     * Returns text region previously highlighted with @ref highlightRegion.
     * @param annotationHandle handle returned by @ref highlightRegion
     * @return text region or null in case of an error.
     */
    TextRange getHighlightedRegion(final Object annotationHandle);

    /**
     * Removes highlighting identified by the annotation handle.
     * @param annotationHandle handle returned by @ref highlightRegion.
     */
    void removeHighlighting(final Object annotationHandle);
}
