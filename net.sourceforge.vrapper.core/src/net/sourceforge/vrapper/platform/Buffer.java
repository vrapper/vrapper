package net.sourceforge.vrapper.platform;

/**
 * Information about the current "buffers".
 * This information is not meant to be cached as it might change every time Vrapper is left.
 * <p><b>Clients are not supposed to implement this interface!</b>
 */
public interface Buffer {
    /** Buffer id, unlike Vim starts from 0. While Eclipse can have multiple tabs with the same
     * input file, the buffer id will be identical for all copied tabs showing the same file.
     */
    public int getId();
    public String getDisplayName();
    boolean isActive();
    boolean isAlternate();
}
