package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Instances of this class are notified when a new EditorAdaptor instance is built.
 * <p>Instances should not assume that this class is used as a singleton, preferably there should
 * be no non-static variables in instances of this class.
 */
public abstract class PlatformVrapperLifecycleAdapter implements PlatformVrapperLifecycleListener {

    @Override
    public void editorInitialized(EditorAdaptor editorAdaptor, boolean enabled) {
    }

    @Override
    public void editorConfigured(EditorAdaptor editorAdaptor, boolean enabled) {
    }

    @Override
    public void editorClosing(EditorAdaptor editorAdaptor, boolean enabled) {
    }
}
