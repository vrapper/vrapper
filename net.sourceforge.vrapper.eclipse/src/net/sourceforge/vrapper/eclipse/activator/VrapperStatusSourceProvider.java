package net.sourceforge.vrapper.eclipse.activator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;

public class VrapperStatusSourceProvider extends AbstractSourceProvider {
    /** Source Id for a boolean flag indicating whether Vrapper is enabled. */
    public static final String SOURCE_ENABLED = "net.sourceforge.vrapper.source.enabled";
    /** Current mode of the last-activated editor. */
    public static final String SOURCE_CURRENTMODE = "net.sourceforge.vrapper.source.currentmode";

    /** The current editor does not seem to be a Vrapper instance, hence the current mode is unknown. */
    public static final String MODE_UNKNOWN = "(unknown mode)";

    private String lastSeenMode = MODE_UNKNOWN;

    public VrapperStatusSourceProvider() {
    }

    @Override
    public void dispose() {
    }

    @Override
    @SuppressWarnings("rawtypes") // Eclipse API uses raw Map...
    public Map getCurrentState() {
        Boolean vrapperEnabled = VrapperPlugin.isVrapperEnabled();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(SOURCE_ENABLED, vrapperEnabled);
        result.put(SOURCE_CURRENTMODE, lastSeenMode);
        return result;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { SOURCE_ENABLED, SOURCE_CURRENTMODE };
    }

    public void fireVrapperEnabledChange(boolean enabled) {
        super.fireSourceChanged(ISources.ACTIVE_EDITOR, SOURCE_ENABLED, enabled);
    }

    public void fireEditorModeChange(EditorMode currentMode) {
        // When Vrapper is not initialized OR when an editor without Vrapper support is focused
        if (currentMode == null) {
            lastSeenMode = MODE_UNKNOWN;

        } else if (currentMode instanceof NormalMode) {
            lastSeenMode = NormalMode.NAME;
        } else if (currentMode instanceof VisualMode) {
            lastSeenMode = VisualMode.NAME;
        } else if (currentMode instanceof InsertMode) {
            lastSeenMode = InsertMode.NAME;
        } else if (currentMode instanceof AbstractCommandLineMode) {
            lastSeenMode = CommandLineMode.NAME;
        } else {
            lastSeenMode = currentMode.getName();
        }
        super.fireSourceChanged(ISources.ACTIVE_EDITOR, SOURCE_CURRENTMODE, lastSeenMode);
    }
}