package net.sourceforge.vrapper.eclipse.platform;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import net.sourceforge.vrapper.eclipse.activator.VrapperStatusSourceProvider;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;
import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * Utility class which holds information about Vrapper's current mode so that not all platform
 * classes need the EditorAdapter injected to get just that.
 */
public class VrapperModeRecorder extends VrapperEventAdapter {

    protected EditorMode currentMode;
    protected boolean vrapperEnabled;

    public VrapperModeRecorder() {
    }

    @Override
    public void modeSwitched(EditorMode oldMode, EditorMode currentMode) {
        this.currentMode = currentMode;

        // Fire listeners on source provider so they know the Vrapper mode was changed
        // This might be triggered for each editor when enabling / disabling Vrapper, but so be it.
        IWorkbench workbench = PlatformUI.getWorkbench();
        ISourceProviderService sourceProviderService =
                (ISourceProviderService) workbench.getService(ISourceProviderService.class);
        VrapperStatusSourceProvider sourceProvider = (VrapperStatusSourceProvider)
                sourceProviderService.getSourceProvider(VrapperStatusSourceProvider.SOURCE_CURRENTMODE);
        sourceProvider.fireEditorModeChange(currentMode);
    }

    @Override
    public void vrapperToggled(boolean enabled) {
        vrapperEnabled = enabled;
    }

    public EditorMode getCurrentMode() {
        return currentMode;
    }
}
