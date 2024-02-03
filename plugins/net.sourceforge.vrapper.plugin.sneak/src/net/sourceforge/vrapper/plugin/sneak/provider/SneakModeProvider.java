package net.sourceforge.vrapper.plugin.sneak.provider;

import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.platform.AbstractPlatformSpecificModeProvider;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakInputMode;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.EditorMode;

public class SneakModeProvider extends AbstractPlatformSpecificModeProvider {

    public SneakModeProvider() {
        super(SneakModeProvider.class.getName());
    }

    @Override
    public List<EditorMode> getModes(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        return Collections.<EditorMode>singletonList(new SneakInputMode(editorAdaptor));
    }
}
