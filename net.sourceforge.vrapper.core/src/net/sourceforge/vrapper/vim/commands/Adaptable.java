package net.sourceforge.vrapper.vim.commands;

/**
 * Instances of this interface can be adapted to another representation. What representations are
 * supported is specific for each instance and is currently not queryable.
 */
public interface Adaptable {
    /**
     * Tries to adapt the current instance to another type. Note that some state (like count values)
     * might be missing in the adapted objects.
     * 
     * @return an object of the requested type or <code>null</code> if not possible.
     */
    public <T> T getAdapter(Class<T> type);
}
