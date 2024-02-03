package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Instances of this interface are notified when a new EditorAdaptor instance is built.
 * <p>Instances should not assume that this class is used as a singleton, preferably there should
 * be no non-static variables in instances of this class.
 * <p>Implementations are recommended to extend {@link PlatformVrapperLifecycleAdapter}.
 */
public interface PlatformVrapperLifecycleListener {

    /** Run when an editor has just read its configuration but before extensions are added. */
    public void editorConfigured(EditorAdaptor editorAdaptor, boolean enabled);

    /** Run when Vrapper is fully initialized in an editor. */
    public void editorInitialized(EditorAdaptor editorAdaptor, boolean enabled);

    /** Called just before closing an editor. */
    public void editorClosing(EditorAdaptor editorAdaptor, boolean enabled);
}
