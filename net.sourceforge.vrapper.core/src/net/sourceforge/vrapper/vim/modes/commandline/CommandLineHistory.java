package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores the command line history and remove duplicates.
 */
public class CommandLineHistory {
	List<String> history = new ArrayList<String>();
	int position = 0;
	String original;

	public void append(String command) {
		history.add(0, command);
		position = -1;
		removeDuplicates();
	}

	private void removeDuplicates() {
		Set<String> contents = new HashSet<String>();
		List<String> tmp = new ArrayList<String>();
		for (String cmd : history) {
			if (contents.contains(cmd))
				continue;
			contents.add(cmd);
			tmp.add(cmd);
		}
		history.clear();
		history.addAll(tmp);
	}

	public void setTemp(String temp) {
		original = temp;
		position = -1;
	}

	/**
	 * Get the previous entry in the history that matches the current
	 * edited command.
	 * @return the command in the history or null if none found to match.
	 */
	public String getPrevious() {
		String result = null;
		while (position >= -1 && (position + 1) < history.size()) {
			position++;
			String tmp = history.get(position);
			if (tmp.startsWith(original)) {
				result = tmp;
				break;
			}
		}
		if (position >= history.size())
			position = history.size() - 1;
		return result;
	}

	/**
	 * Get the next entry in the history that matches the current
	 * edited command.
	 * @return the command in the history or the original command if none
	 * found to match.
	 */
	public String getNext() {
		String result = original;
		position--;
		while (position >= 0 && position < history.size()) {
			String tmp = history.get(position);
			position--;
			if (tmp.startsWith(original)) {
				result = tmp;
				break;
			}
		}
		if (position < 0)
			position = -1;
		return result;
	}
}
