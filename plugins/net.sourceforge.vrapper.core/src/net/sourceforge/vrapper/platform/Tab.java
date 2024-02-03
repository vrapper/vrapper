package net.sourceforge.vrapper.platform;

/** Information about a tab.
 * This information is not meant to be cached as it might change every time Vrapper is left.
 * <p><b>Clients are not supposed to implement this interface!</b>
 */
public interface Tab {
    /** Tab number, unlike Vim starts with 0. Note that this number can change at any time! */
    public int getNumber();
    public String getDisplayName();
    public boolean isActive();
}
