package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

public class ToggleFoldingCommand extends CountIgnoringNonRepeatableCommand {
	
	public static final String EXPAND = "org.eclipse.ui.edit.text.folding.expand";
	public static final String COLLAPSE = "org.eclipse.ui.edit.text.folding.collapse";

	/** An instance using the default Eclipse command ids. */
	public static final Command DEFAULTINSTANCE = new ToggleFoldingCommand(EXPAND, COLLAPSE);

	private String expandCmdId;
	private String collapseCmdId;
	
	public ToggleFoldingCommand(String expandCmdId, String collapseCmdId) {
		this.expandCmdId = expandCmdId;
		this.collapseCmdId = collapseCmdId;
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		int before = editorAdaptor.getViewContent().getNumberOfLines();
		EclipseCommand.doIt(expandCmdId, editorAdaptor, false);
		int after = editorAdaptor.getViewContent().getNumberOfLines();
		if (before == after) {
			EclipseCommand.doIt(collapseCmdId, editorAdaptor, false);
		}
	}

}
