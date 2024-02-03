package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class VisualFindFileCommand extends CountIgnoringNonRepeatableCommand {
	
	public static final VisualFindFileCommand INSTANCE = new VisualFindFileCommand();

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		editorAdaptor.rememberLastActiveSelection();
		TextObject selection = editorAdaptor.getSelection();
		
		String filename = editorAdaptor.getModelContent()
				.getText(selection.getRegion(editorAdaptor, NO_COUNT_GIVEN));
		
		FindFileCommand findFile = new FindFileCommand(filename);
		findFile.execute(editorAdaptor);
		LeaveVisualModeCommand.doIt(editorAdaptor);
	}

}
