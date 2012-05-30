package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the command line history for each mode.
 */
public class CommandLineHistory {
	Map<String, ArrayList<String>> history = new HashMap<String, ArrayList<String>>();
	int index = -1;
	String original;
	String mode;
	
	public static final CommandLineHistory INSTANCE = new CommandLineHistory();
	
	//singleton
	private CommandLineHistory() { }
	
	private List<String> getModeHistory() {
		if( ! history.containsKey(mode)) {
			history.put(mode, new ArrayList<String>());
		}
		return history.get(mode);
	}
	
	public void setMode(String modeName) {
		mode = modeName;
		index = -1;
	}

	public void append(String command) {
		List<String> modeHistory = getModeHistory();
		//remove duplicates (if any)
		if(modeHistory.contains(command)) {
			modeHistory.remove(command);
		}
		
		modeHistory.add(0, command);
		index = -1;
	}

	public void setTemp(String temp) {
		original = temp;
		index = -1;
	}

	/**
	 * Get the previous entry in the history that matches the current
	 * edited command.
	 * @return the command in the history or null if none found to match.
	 */
	public String getPrevious() {
		List<String> modeHistory = getModeHistory();
		String command;
		for(int i=index+1; i < modeHistory.size(); i++) {
			command = modeHistory.get(i);
			if(command.startsWith(original)) {
				index = i;
				return command;
			}
		}
		index = modeHistory.size() -1;
		return null;
	}

	/**
	 * Get the next entry in the history that matches the current
	 * edited command.
	 * @return the command in the history or the original command if none
	 * found to match.
	 */
	public String getNext() {
		List<String> modeHistory = getModeHistory();
		String command;
		for(int i=index-1; i > -1; i--) {
			command = modeHistory.get(i);
			if(command.startsWith(original)) {
				index = i;
				return command;
			}
		}
		index = -1;
		return original;
	}
}
