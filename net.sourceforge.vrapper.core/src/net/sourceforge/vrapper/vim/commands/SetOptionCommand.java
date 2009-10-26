package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SetOptionCommand<T> extends ConfigCommand<T> {

    private final T value;

    public SetOptionCommand(Option<T> option, T value) {
        super(option);
        this.value = value;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        editorAdaptor.getConfiguration().set(option, value);
    }

}
