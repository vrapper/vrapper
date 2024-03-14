package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration.Option;

public abstract class ConfigCommand<T> extends CountIgnoringNonRepeatableCommand {

    protected final Option<T> option;

    public ConfigCommand(Option<T> option) {
        this.option = option;
    }
}
