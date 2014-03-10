package net.sourceforge.vrapper.plugin.surround.provider;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.eclipse.mode.AbstractEclipseSpecificModeProvider;
import net.sourceforge.vrapper.plugin.surround.mode.ReplaceDelimiterMode;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class SurroundModesProvider extends AbstractEclipseSpecificModeProvider {

    public SurroundModesProvider() {
        super("Surround modes provider");
    }

    @Override
    public List<EditorMode> getModes(EditorAdaptor editorAdaptor) {
        ArrayList<EditorMode> modes = new ArrayList<EditorMode>();
        modes.add(new ReplaceDelimiterMode(editorAdaptor));
        return modes;
    }

}
