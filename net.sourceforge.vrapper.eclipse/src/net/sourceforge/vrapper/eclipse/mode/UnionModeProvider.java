package net.sourceforge.vrapper.eclipse.mode;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class UnionModeProvider extends AbstractEclipseSpecificModeProvider {

    private List<AbstractEclipseSpecificModeProvider> providers;

    public UnionModeProvider(String name, List<AbstractEclipseSpecificModeProvider> providers) {
        super(name);
        this.providers = providers;
    }

    @Override
    public List<EditorMode> getModes(EditorAdaptor editorAdaptor) {
        List<EditorMode> extensionModes = new ArrayList<EditorMode>();
        for (AbstractEclipseSpecificModeProvider provider : providers) {
            extensionModes.addAll(provider.getModes(editorAdaptor));
        }
        return extensionModes;
    }

}
