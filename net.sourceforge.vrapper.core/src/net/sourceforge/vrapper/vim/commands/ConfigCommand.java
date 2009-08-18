package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ConfigCommand<T> extends CountIgnoringNonRepeatableCommand {

    private final Option<T> option;
    private final T value;

    public ConfigCommand(Option<T> option, T value) {
        super();
        this.option = option;
        this.value = value;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.getConfiguration().set(option, value);
    }
}
