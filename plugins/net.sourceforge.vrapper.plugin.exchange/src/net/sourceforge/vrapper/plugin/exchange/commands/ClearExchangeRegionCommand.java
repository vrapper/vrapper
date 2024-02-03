package net.sourceforge.vrapper.plugin.exchange.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

public class ClearExchangeRegionCommand extends
        CountIgnoringNonRepeatableCommand {
    
    public final static ClearExchangeRegionCommand INSTANCE = new ClearExchangeRegionCommand();

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        ExchangeOperation.INSTANCE.clear(editorAdaptor);
    }

}
