package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class OpenFileCommand extends CountIgnoringNonRepeatableCommand {
	
	private String filename;
	
	public OpenFileCommand(String filename) {
		this.filename = filename;
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		
        boolean success = editorAdaptor.getFileService().openFile(filename);
        if(! success) {
        	editorAdaptor.getUserInterfaceService().setErrorMessage("Could not open file: " + filename);
        }
	}

}
