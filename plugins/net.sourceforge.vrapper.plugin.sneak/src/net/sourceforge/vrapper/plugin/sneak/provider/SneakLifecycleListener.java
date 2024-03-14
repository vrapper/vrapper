package net.sourceforge.vrapper.plugin.sneak.provider;

import net.sourceforge.vrapper.platform.PlatformVrapperLifecycleAdapter;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.EditorAdaptorStateManager;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakCommandListener;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SneakLifecycleListener extends PlatformVrapperLifecycleAdapter {

    @Override
    public void editorConfigured(EditorAdaptor editorAdaptor, boolean enabled) {
        editorAdaptor.addVrapperEventListener(new SneakCommandListener(editorAdaptor,
                new EditorAdaptorStateManager()));

        // [TODO] Check default bindings
    }
}
