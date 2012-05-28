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

	public void append(String command) {
		history.add(0, command);
		position = 0;
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
		history.add(0, temp);
		position++;
		removeDuplicates();
	}

	public String getPrevious() {
		if (position < history.size()) {
			String result = history.get(position);
			position++;
			if (position == history.size())
				position--;
			return result;
		}
		return "";
	}

	public String getNext() {
		if (position >= 0 && position < history.size()) {
			String result = history.get(position);
			position--;
			if (position == -1)
				position++;
			return result;
		}
		return "";
	}
}
