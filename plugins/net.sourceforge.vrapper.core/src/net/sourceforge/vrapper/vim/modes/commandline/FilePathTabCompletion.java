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
	public String getNextMatch(String prefix, boolean searchPath, boolean dirsOnly, boolean reverse) {
		//first time through, or user modified the string
		if(lastAttempt == null || ! lastAttempt.equals(prefix)) {
			init(prefix);
		}
		
		//find next match after lastAttempt
		String match;
		if(searchPath) {
			String[] paths = editorAdaptor.getConfiguration().get(Options.PATH).split(",");
			for(int i=0; i < paths.length; i++) {
				if("".equals(paths[i])) { // "" is actually cwd, resolve now
					paths[i] = getStartDir("");
				}
			}
			match = editorAdaptor.getFileService().findFileInPath(original, lastAttempt, reverse, paths);
		}
		else if(dirsOnly) {
			match = editorAdaptor.getFileService().getDirPathMatch(original, lastAttempt, reverse, getStartDir(original));
		}
		else {
			match = editorAdaptor.getFileService().getFilePathMatch(original, lastAttempt, reverse, getStartDir(original));
		}
		
		/**
		 * **** I don't know how to make this cleaner!!! ****
		 * In Vim, every time you hit <tab> it goes on to the next match.  Once you've
		 * gone through all the matches, it displays the "prefix" that you originally
		 * typed. However, if you hit <tab> and there was exactly one match then it
		 * continues to display the one match rather than displaying the "prefix".
		 * Except, if that one match is a directory, the next <tab> goes into that
		 * directory rather than continuing to display the one match.  So, if the next
		 * match is the prefix (we've wrapped around all possible matches) and there
		 * was exactly one match prior to that, see if we can go into that directory.
		 * This gets painful when trying to support ../<tab> because there is always
		 * exactly one '../' so it's a bunch of special cases.
		 */
		if(match.equals(original)) {
			//if we've looped back around, restart
			lastAttempt = null;
			
			//if there's exactly one match,
			//start iterating within that directory rather than restarting
			if(numMatches == 1) {
				init(match);
				return getNextMatch(prefix, searchPath, dirsOnly, reverse);
			}
		}
		else {
			//prepare for next iteration
			lastAttempt = match;
			numMatches++;
		}
		
		return match;
	}
	
	private String getStartDir(String prefix) {
		if(prefix.startsWith("/")) {
			return "";
		}
		
		if(editorAdaptor.getConfiguration().get(Options.AUTO_CHDIR)) {
			return editorAdaptor.getFileService().getCurrentFilePath();
		}
		
		return editorAdaptor.getRegisterManager().getCurrentWorkingDirectory();
	}
}
