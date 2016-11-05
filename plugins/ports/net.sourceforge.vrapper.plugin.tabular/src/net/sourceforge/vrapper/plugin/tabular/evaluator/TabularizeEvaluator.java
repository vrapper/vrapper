package net.sourceforge.vrapper.plugin.tabular.evaluator;

import static net.sourceforge.vrapper.vim.commands.CommandWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.seq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.plugin.tabular.commands.FormatSpecifier;
import net.sourceforge.vrapper.plugin.tabular.commands.FormatSpecifier.AlignmentMode;
import net.sourceforge.vrapper.plugin.tabular.commands.ShowMessageCommand;
import net.sourceforge.vrapper.plugin.tabular.commands.TabularizeCommand;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineHistory;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public final class TabularizeEvaluator implements Evaluator {

	private static final String NO_LAST_CMD_ERROR = "Tabularize hasn't been called yet; no pattern/command to reuse!";

	// EXAMPLE: "Tabularize  /,/l1r1c0" or "Tabularize pattern_name"
	private final Pattern TABULARIZE_CMD = Pattern.compile("^\\W*\\w+ +(\\w+)?(?: */(.*))?");
	private final int TABULARIZE_CMD_NAME_GROUP = 1;
	private final int TABULARIZE_CMD_DELIMITER_SPEC_GROUP = 2;

	private final Pattern DELIM_SPECIFIER = Pattern.compile("^((?:\\\\/|[^/])*)(?:/((?:[lrc][0-9]+)+))?$");
	private final int DELIM_SPECIFIER_PATTERN_GROUP = 1; 
	private final int DELIM_SPECIFIER_FORMATS_GROUP = 2; 

	private final Pattern FORMAT_SPECIFIER = Pattern.compile("([lcr])([0-9]+)");
	private final int FORMAT_SPECIFIER_ALIGNMENT_MODE_GROUP = 1;
	private final int FORMAT_SPECIFIER_EXTRA_SPACES_GROUP = 2; 

	private Map<String, Command> namedCommands = new HashMap<String, Command>();
	private Command lastCommand = new ShowMessageCommand(NO_LAST_CMD_ERROR, true);
	
	private static TabularizeEvaluator instance;
	
	private TabularizeEvaluator() {
		// singleton
	}
	
	public static TabularizeEvaluator getInstance() {
		if (instance == null)
			instance = new TabularizeEvaluator();
		return instance;
	}
	
	public void registerPattern(String name, String pattern) {
		Command cmd = parsePattern(pattern).buildCommand();
		namedCommands.put(name, cmd);
	}

	@Override
	public Object evaluate(EditorAdaptor vim, Queue<String> commandTokens) throws CommandExecutionException {
		if (commandTokens.isEmpty()) {
			lastCommand.execute(vim);
			return null;
		}
		
		/* Get the command from the history because whitespace may be a part of the regex */
		String commandStr = CommandLineHistory.INSTANCE.getPrevious();
		CommandLineHistory.INSTANCE.getNext();

		Command command = parseTabularizeCmd(commandStr).buildCommand();
		try {
			lastCommand = command;
			command.execute(vim);
		} catch (CommandExecutionException e) {
			vim.getUserInterfaceService().setErrorMessage("Tabularize: Something went wrong!");
		} 
		return null;
	}
	
	private ParseResult parseTabularizeCmd(String entireCmd) {
		Matcher cmdMatcher = TABULARIZE_CMD.matcher(entireCmd);
		if (!cmdMatcher.matches()) {
			return ParseResult.failure(String.format("Tabularize: Invalid command '%s'", entireCmd));
		}

		final boolean IS_NAMED_PATTERN = entireCmd.indexOf("/") == -1;
		if (IS_NAMED_PATTERN) {
			String name = cmdMatcher.group(TABULARIZE_CMD_NAME_GROUP);
			return ParseResult.withName(name);
		} else {
			String delimiterPatternAndFormats = cmdMatcher.group(TABULARIZE_CMD_DELIMITER_SPEC_GROUP);
			return parsePattern(delimiterPatternAndFormats);
		}
	}
	
	// Parses the part of the command after the forward slash (/), which is the pattern and optional format specifiers.
	private ParseResult parsePattern(String pattern) {
		final Matcher cmdMatcher = DELIM_SPECIFIER.matcher(pattern);
		if (!cmdMatcher.matches())
			return ParseResult.failure(String.format("Tabularize: Unrecognized command '%s'", pattern));

		String delimiter;
		final ArrayList<FormatSpecifier> formats;

		delimiter = cmdMatcher.group(DELIM_SPECIFIER_PATTERN_GROUP);
		if (cmdMatcher.group(DELIM_SPECIFIER_FORMATS_GROUP) != null) {
			formats = parseFormatSpecifiers(cmdMatcher.group(DELIM_SPECIFIER_FORMATS_GROUP));
		} else {
			formats = new ArrayList<FormatSpecifier>();
		}
		delimiter = unescapeForwardSlashes(delimiter);
		
		return ParseResult.withDelimiterRegex(delimiter, formats);
	}
	
	private static String unescapeForwardSlashes(String pattern) {
		return pattern.replace("\\/", "/");
	}
	
	// This method is called only when we know we have a valid list of format specifiers 
	private ArrayList<FormatSpecifier> parseFormatSpecifiers(String formatSpecifierStr) {
		ArrayList<FormatSpecifier> formats = new ArrayList<FormatSpecifier>();
		Matcher formatMatcher = FORMAT_SPECIFIER.matcher(formatSpecifierStr);
		while (formatMatcher.find()) {
			String alignmentModeChar = formatMatcher.group(FORMAT_SPECIFIER_ALIGNMENT_MODE_GROUP);
			int extraSpaces = Integer.parseInt(formatMatcher.group(FORMAT_SPECIFIER_EXTRA_SPACES_GROUP));
			FormatSpecifier format = new FormatSpecifier();
			format.setAlignmentMode(AlignmentMode.fromChar(alignmentModeChar));
			format.setNumberOfExtraSpaces(extraSpaces);
			formats.add(format);
		}
		return formats;
	}
	
	private static abstract class ParseResult {
		
		abstract Command buildCommand();
		
		static ParseResult withName(String patternName) {
			return new NamedPattern(patternName);
		}
		
		static ParseResult withDelimiterRegex(String delimiterRegex, ArrayList<FormatSpecifier> formats) {
			return new UnNamedPattern(delimiterRegex, formats);
		}
		
		static ParseResult failure(String errorMsg) {
			return new ParseError(errorMsg);
		}
		
	}
	
	private static class UnNamedPattern extends ParseResult {
		
		private final String delimiterRegex;
		private final ArrayList<FormatSpecifier> formats;
		
		UnNamedPattern(String delimiterRegex, ArrayList<FormatSpecifier> formats) {
            this.delimiterRegex = delimiterRegex;
            this.formats = formats;
		}

		@Override
		Command buildCommand() {
			Pattern delimiterPattern;
			String msg;
			try {
				delimiterPattern = Pattern.compile(delimiterRegex);
				msg = String.format("Tabular: delimiter is regular expression [%s]", delimiterRegex);
			} catch (PatternSyntaxException e) {
				delimiterPattern = Pattern.compile(Pattern.quote(delimiterRegex));
				msg = String.format("Tabular: delimiter is literal string [%s]", delimiterRegex);
			}
			
			Command showInfo = new ShowMessageCommand(msg);
			Command tabularize = new TabularizeCommand(delimiterPattern, formats);

			return dontRepeat(seq(showInfo, tabularize));
		}

	}
	
	private static class NamedPattern extends ParseResult {
		
		private final String name;
		
		NamedPattern(String name) {
			this.name = name;
		}
		
		@Override
		Command buildCommand() {
			Command cmd = TabularizeEvaluator.getInstance().namedCommands.get(name);
			return cmd != null ? cmd : new ShowMessageCommand("Tabularize: No command with name '" + name + "'", true);
		}
		
	}
	
	private static class ParseError extends ParseResult {
		
		private String errorMessage;
		
		ParseError(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		
		@Override
		Command buildCommand() {
			return new ShowMessageCommand(errorMessage, true);
		}
		
	}

}
