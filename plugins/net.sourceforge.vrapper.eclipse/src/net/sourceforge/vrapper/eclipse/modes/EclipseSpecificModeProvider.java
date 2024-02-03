package net.sourceforge.vrapper.eclipse.modes;

import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.platform.AbstractPlatformSpecificModeProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class EclipseSpecificModeProvider extends AbstractPlatformSpecificModeProvider
        implements PlatformSpecificModeProvider {

    public EclipseSpecificModeProvider() {
        super("Vrapper-Eclipse Specific Modes Provider");
    }

    @Override
    public List<EditorMode> getModes(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        return Collections.singletonList((EditorMode) new InsertExpandMode(editorAdaptor));
    }
}
