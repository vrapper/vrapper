package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;

public class ToggleLocalOptionCommand extends ConfigCommand<Boolean> {

    public ToggleLocalOptionCommand(Option<Boolean> option) {
        super(option);
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        Boolean value = editorAdaptor.getConfiguration().get(option);
        editorAdaptor.getConfiguration().setLocal(option, !value);
    }

}
