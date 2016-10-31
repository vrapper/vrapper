package net.sourceforge.vrapper.plugin.tabular.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

public class ShowMessageCommand extends CountIgnoringNonRepeatableCommand {
	
	private String message;
	private boolean isError;

	public ShowMessageCommand(String msg, boolean isError) {
		this.message = msg;
		this.isError = isError;
	}

	public ShowMessageCommand(String msg) {
		this.message = msg;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		if (isError)
			editorAdaptor.getUserInterfaceService().setErrorMessage(message);
		else
			editorAdaptor.getUserInterfaceService().setInfoMessage(message);
	}

}
