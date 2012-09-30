package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class FindFileCommand extends CountIgnoringNonRepeatableCommand {
	
	private String filename;
	
	public FindFileCommand(String filename) {
		this.filename = filename;
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		
		String[] paths = editorAdaptor.getConfiguration().get(Options.PATH).split(",");
        boolean success = editorAdaptor.getFileService().findAndOpenFile(filename, paths);
        if(! success) {
        	editorAdaptor.getUserInterfaceService().setErrorMessage("Could not find file: " + filename);
        }
	}
}
