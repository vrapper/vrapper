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
		if(! editorAdaptor.getFileService().openFile(filename)) {
			//if file doesn't exist, this is a 'create' operation
			if(editorAdaptor.getFileService().createFile(filename)) {
				//if create succeeded, immediately open the file
				if(editorAdaptor.getFileService().openFile(filename)) {
					editorAdaptor.getUserInterfaceService().setInfoMessage("\""+filename+"\" [New File]");
				}
				else {
					editorAdaptor.getUserInterfaceService().setErrorMessage("Could not open file: " + filename);
				}
			}
			else { //couldn't open or create this file
				editorAdaptor.getUserInterfaceService().setErrorMessage("Could not open file: " + filename);
			}
		}
	}

}
