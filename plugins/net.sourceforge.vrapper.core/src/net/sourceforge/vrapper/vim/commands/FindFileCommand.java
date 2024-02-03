package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class FindFileCommand extends CountIgnoringNonRepeatableCommand {
	
	public static final FindFileCommand INSTANCE = new FindFileCommand();
	
	private String filename;
	
	/**
	 * This constructor is used by the 'gf' command
	 */
	private FindFileCommand() {
		this.filename = null;
	}
	
	/**
	 * This constructor is used by the command-line ':find' command
	 * And also the visual verson of the 'gf' command
	 */
	public FindFileCommand(String filename) {
		this.filename = filename.trim();
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		
		if(filename == null) {
			filename = VimUtils.getWordUnderCursor(editorAdaptor, true);
		}
		
		String[] paths = editorAdaptor.getConfiguration().get(Options.PATH).split(",");
        boolean success = editorAdaptor.getFileService().findAndOpenFile(filename, paths);
        if(! success) {
        	editorAdaptor.getUserInterfaceService().setErrorMessage("Can't find file \"" + filename + "\" in path");
        }
        
        //reset filename since this instance is
        //reused when set to INSTANCE
        filename = null;
	}
}
