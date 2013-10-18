package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

public class ToggleFoldingCommand extends CountIgnoringNonRepeatableCommand {

	public static final Command INSTANCE = new ToggleFoldingCommand();
	
	public static final String EXPAND = "org.eclipse.ui.edit.text.folding.expand";
	public static final String COLLAPSE = "org.eclipse.ui.edit.text.folding.collapse";
	
	private ToggleFoldingCommand() { /* singleton */ }

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		int before = editorAdaptor.getViewContent().getNumberOfLines();
		EclipseCommand.doIt(Command.NO_COUNT_GIVEN, EXPAND, editorAdaptor, false);
		int after = editorAdaptor.getViewContent().getNumberOfLines();
		if (before == after) {
			EclipseCommand.doIt(Command.NO_COUNT_GIVEN, COLLAPSE, editorAdaptor, false);
		}
	}

}
