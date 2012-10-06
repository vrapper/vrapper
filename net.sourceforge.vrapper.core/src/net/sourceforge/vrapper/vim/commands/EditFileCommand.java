package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class EditFileCommand extends CountIgnoringNonRepeatableCommand {
	
	private String filename;
	
	public EditFileCommand(String filename) {
		this.filename = filename;
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		
		//if not an absolute path
		if(!filename.startsWith("/")) {
			if(editorAdaptor.getConfiguration().get(Options.AUTO_CHDIR)) {
				filename = editorAdaptor.getFileService().getCurrentFilePath() + "/" + filename;
			}
			else {
				filename = editorAdaptor.getRegisterManager().getCurrentWorkingDirectory() + "/" + filename;
			}
		}
        boolean success = editorAdaptor.getFileService().openFile(filename);
        if(! success) {
        	editorAdaptor.getUserInterfaceService().setErrorMessage("Could not open file: " + filename);
        }
	}

}
