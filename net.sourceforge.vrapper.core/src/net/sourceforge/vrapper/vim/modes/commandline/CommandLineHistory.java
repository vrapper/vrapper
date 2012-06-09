package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the command line history for each mode.
 */
public class CommandLineHistory {
	//history of commands for each command-line mode
	private Map<String, ArrayList<String>> modeHistory = new HashMap<String, ArrayList<String>>();
	//current index into history
	private int index = -1;
	//original text entered by user before scrolling through history
	private String original;
	//ordered list of previously-entered commands for the current Mode
	private List<String> history;
	
	public static final CommandLineHistory INSTANCE = new CommandLineHistory();
	
	//singleton
	private CommandLineHistory() { }
	
	/**
	 * We've changed modes.  Fetch the history for this mode.
	 * @param modeName name of now-current mode
	 */
	public void setMode(String modeName) {
		index = -1;
		if( ! modeHistory.containsKey(modeName)) {
			modeHistory.put(modeName, new ArrayList<String>());
		}
		history = modeHistory.get(modeName);
	}

	/**
	 * User has committed a command (hit 'enter').  Add it to the history.
	 * @param command - command to add to history
	 */
	public void append(String command) {
		//remove duplicates (if any)
		if(history.contains(command)) {
			history.remove(command);
		}
		
		history.add(0, command);
		index = -1;
	}

	/**
	 * The user entered a couple keys then started scrolling
	 * through the command history.  Those keys aren't actually
	 * part of the history (not committed yet) but they're
	 * treated as the first item in the history.
	 * @param temp - User-entered string, may not be a full command
	 */
	public void setTemp(String temp) {
		original = temp;
		index = -1;
	}

	/**
	 * Get the previous entry in the history that starts with
	 * whatever the user had entered before scrolling through the history.
	 * @return the command in the history or null if none found to match.
	 */
	public String getPrevious() {
		String command;
		for(int i=index+1; i < history.size(); i++) {
			command = history.get(i);
			if(command.startsWith(original)) {
				index = i;
				return command;
			}
		}
		return null;
	}

	/**
	 * Get the next entry in the history that starts with
	 * whatever the user had entered before scrolling through the history.
	 * @return the command in the history or the original command if none
	 * found to match.
	 */
	public String getNext() {
		String command;
		for(int i=index-1; i > -1; i--) {
			command = history.get(i);
			if(command.startsWith(original)) {
				index = i;
				return command;
			}
		}
		index = -1;
		return original;
	}
}
