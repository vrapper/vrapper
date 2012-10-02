package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.LineInformation;
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
			filename = getFileUnderCursor(editorAdaptor);
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
	
	//there has to be a better way to do this
	//but I can't think of a clever regex to do what I need
	private final String filenameChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/.-_+#$%~=";
	
	private String getFileUnderCursor(EditorAdaptor editorAdaptor) {
		int cursor = editorAdaptor.getCursorService().getPosition().getModelOffset();
		LineInformation line = editorAdaptor.getModelContent().getLineInformationOfOffset(cursor);
		String beforeCursor = editorAdaptor.getModelContent().getText(line.getBeginOffset(), cursor - line.getBeginOffset());
		String afterCursor = editorAdaptor.getModelContent().getText(cursor, line.getEndOffset() - cursor);
		
		StringBuffer filename = new StringBuffer();
		for(int i=beforeCursor.length()-1; i >= 0; i--) {
			if(filenameChars.contains(""+beforeCursor.charAt(i))) {
				filename.insert(0, beforeCursor.charAt(i));
			}
			else {
				//break on first non-filenameChar
				break;
			}
		}
		for(int i=0; i < afterCursor.length(); i++) {
			if(filenameChars.contains(""+afterCursor.charAt(i))) {
				filename.append(afterCursor.charAt(i));
			}
			else {
				//break on first non-filenameChar
				break;
			}
		}
		
		return filename.toString();
	}
}
