package net.sourceforge.vrapper.eclipse.mode;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.platform.AbstractPlatformSpecificModeProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class UnionModeProvider extends AbstractPlatformSpecificModeProvider {

    private List<AbstractPlatformSpecificModeProvider> providers;

    public UnionModeProvider(String name, List<AbstractPlatformSpecificModeProvider> providers) {
        super(name);
        this.providers = providers;
    }

    @Override
    public List<EditorMode> getModes(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        List<EditorMode> extensionModes = new ArrayList<EditorMode>();
        for (AbstractPlatformSpecificModeProvider provider : providers) {
            extensionModes.addAll(provider.getModes(editorAdaptor));
        }
        return extensionModes;
    }

}
