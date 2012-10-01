package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

/**
 * The user typed ":e <partial>" then hit TAB.  Iterate through all
 * possible matches for that partial file path.
 */
public class FilePathTabCompletion {
	
	private EditorAdaptor editorAdaptor;
	private String original = null;
	private String lastAttempt = null;
	private int numMatches = 0;

	public FilePathTabCompletion(EditorAdaptor editorAdaptor) {
		this.editorAdaptor = editorAdaptor;
	}
	
	private void init(String prefix) {
		original = prefix;
		numMatches = 0;
		lastAttempt = null;
	}
	
	/**
	 * Given a partial file path, return the closest match
	 */
	public String getNextMatch(String prefix, boolean searchPath) {
		//first time through, or user modified the string
		if(lastAttempt == null || ! lastAttempt.equals(prefix)) {
			init(prefix);
		}
		
		//find next match after lastAttempt
		String match;
		if(searchPath) {
			String[] paths = editorAdaptor.getConfiguration().get(Options.PATH).split(",");
			match = editorAdaptor.getFileService().findFileInPath(original, lastAttempt, paths);
		}
		else {
			match = editorAdaptor.getFileService().getFilePathMatch(original, lastAttempt);
		}
		
		if(match.equals(original)) {
			//if we've looped back around, restart
			lastAttempt = null;
			
			//if there's exactly one match,
			//start iterating within that directory rather than restarting
			if(numMatches == 1) {
				init(match);
				return getNextMatch(prefix, searchPath);
			}
		}
		else {
			//prepare for next iteration
			lastAttempt = match;
			numMatches++;
		}
		
		return match;
	}
}
