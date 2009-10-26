package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Command that wraps other one and executes it only if an option has value equal to triggerValue.
 * @param <T> triggering option's value type
 */
public class OptionDependentCommand<T> implements Command {

    private final Option<T> option;
    private final String triggerValue;
    private final Command wrapped;

    public OptionDependentCommand(Option<T> option, String triggerValue, Command wrapped) {
        this.option = option;
        this.triggerValue = triggerValue;
        this.wrapped = wrapped;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        if (editorAdaptor.getConfiguration().get(option).equals(triggerValue))
            wrapped.execute(editorAdaptor);
    }

    public Command repetition() {
        return null;
    }

    public int getCount() {
        return wrapped.getCount();
    }

    public Command withCount(int count) {
        return this;
    }
}
