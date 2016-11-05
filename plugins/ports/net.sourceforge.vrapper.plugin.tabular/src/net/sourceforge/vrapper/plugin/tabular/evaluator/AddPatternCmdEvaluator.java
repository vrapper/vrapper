package net.sourceforge.vrapper.plugin.tabular.evaluator;

import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineHistory;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public class AddPatternCmdEvaluator implements Evaluator {

	private final Pattern ADD_PATTERN_CMD = Pattern.compile("\\W*\\w+ +(\\w+)[^/]*/(.*)");
	private final int NAME_GROUP = 1;
	private final int PATTERN_GROUP = 2;

	@Override
	public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
		/* Get the command from the history because whitespace may be a part of the regex */
		String commandStr = CommandLineHistory.INSTANCE.getPrevious();
		CommandLineHistory.INSTANCE.getNext();

		Matcher cmdMatcher = ADD_PATTERN_CMD.matcher(commandStr);
		if (!cmdMatcher.matches()) {
			vim.getUserInterfaceService().setErrorMessage("AddTabularPattern: Invalid syntax");
			return null;
		}
		
		String name = cmdMatcher.group(NAME_GROUP);
		String pattern = cmdMatcher.group(PATTERN_GROUP);

		TabularizeEvaluator.getInstance().registerPattern(name, pattern);
		vim.getUserInterfaceService().setInfoMessage(String.format("AddTabularPattern: Added '%s' named pattern", name));

		return null;
	}

}
