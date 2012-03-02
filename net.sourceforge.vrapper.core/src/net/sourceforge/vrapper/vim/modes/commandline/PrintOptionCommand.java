package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;

/** Prints a configuration value to the UI info-message. */
public class PrintOptionCommand<T> extends ConfigCommand<T> {
    
    public PrintOptionCommand(Option<T> option) {
        super(option);
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.getUserInterfaceService().setErrorMessage("");
        editorAdaptor.getUserInterfaceService().setInfoMessage(option.getId() + ": ["
            + editorAdaptor.getConfiguration().get(option) + "]");
    }
}
