package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Takes a user-defined String such as:
 * <pre>
 * :g/^a/normal wdw
 * :g/something/s/foo/bar/g
 * </pre>
 * and parses all the pieces.  The expected pieces are:
 * <ul>
 *     <li>'g', 'g!', or 'v' to determine whether we find matches or non-matches</li>
 *     <li>/{pattern}/ to determine what we're matching on</li>
 *     <li>command name (e.g., 'normal', 's', 'd', etc.)</li>
 *     <li>any command args (e.g., 'wdw' or '/foo/bar/g')</li>
 * </ul>
 *
 * We take those pieces and generate a Command if everything is valid.
 */
public class ExCommandOperation extends AbstractLinewiseOperation {

	protected static final String NEXTLINE_MARK = CursorService.INTERNAL_MARK_PREFIX + "-ex-nextline";

	String originalDefinition;

	public ExCommandOperation(String definition) {
		this.originalDefinition = definition;
	}

	public TextOperation repetition() {
		//The dot command doesn't work for "normal", it leaves NormalMode
		//in a bad state. So, prevent the dot command on "normal".  This
		//means dot will actually perform whatever operation was last in
		//the "normal" command execution.
		//Surprisingly, this is consistent with vim's behavior.
		if(originalDefinition.contains("normal ")) {
			return null;
		}
		else {
			return this;
		}
	}

	@Override
	public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
			throws CommandExecutionException {
		return SimpleLineRange.entireFile(editorAdaptor);
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, LineRange lineRange)
			throws CommandExecutionException {
		boolean findMatch = true;
		//leave 'originalDefinition' untouched so repetition can use it again
		//we'll be modifying 'definition' to make parsing easier
		String definition = originalDefinition;
		if(definition.startsWith("g!")) {
			findMatch = false;
			//chop off 'g!'
			definition = definition.substring(2);
		}
		else if(definition.startsWith("v")) {
			findMatch = false;
			//chop off 'v'
			definition = definition.substring(1);
		}
		else if(definition.startsWith("g")) {
			findMatch = true;
			//chop off 'g'
			definition = definition.substring(1);
		}
		else { //doesn't start with a 'g' or 'v'?  How'd we get here?
			VrapperLog.error("Expected a g or v instruction but got [" + originalDefinition + "]");
			throw new CommandExecutionException("Parsing error");
		}

		//a search pattern must be defined but it doesn't have to be '/'
		//whatever character is after 'g', 'g!', or 'v' must be pattern delimiter
		char delimiter = definition.charAt(0);

		//delimiter is at 1, where is it's match?
		int patternEnd = definition.indexOf(delimiter, 1);
		if(patternEnd == -1) {
			//pattern didn't end
			throw new CommandExecutionException("Missing separator!");
		}

		//grab text between delimiters
		String pattern = definition.substring(1, patternEnd);

		if (pattern.length() == 0) {
			// If no pattern defined, use last search.
			// Register manager guarantees that the register content is not null here.
			pattern = editorAdaptor.getRegisterManager().getRegister("/").getContent().getText();
			if (pattern.length() == 0) {
				throw new CommandExecutionException("No search pattern given and no active search!");
			}
		}
		Pattern regex = Pattern.compile(pattern);

		if (definition.length() <= patternEnd + 1) {
			// pattern was defined, but no command
			throw new CommandExecutionException("No ex command to execute on pattern match!");
		}

		//chop off pattern (+delimiter), all that should be left is command
		definition = definition.substring(patternEnd + 1);

		LineWiseOperation operation = buildExCommand(definition, editorAdaptor);

		if(operation != null) {
			executeExCommand(lineRange, findMatch, regex, operation, editorAdaptor);
		}
	}

	private LineWiseOperation buildExCommand(String command, EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
		if(command.startsWith("normal ")) {
			String args = command.substring("normal ".length());
			return new AnonymousMacroOperation(args);
		}
		else if(command.startsWith("s")) {
			SubstitutionDefinition definition;
			try {
				definition = new SubstitutionDefinition(command,
						editorAdaptor.getRegisterManager());
			} catch (IllegalArgumentException e) {
				throw new CommandExecutionException(e.getMessage());
			}
			if (definition.hasFlag('c')) {
				throw new CommandExecutionException("Cannot use 'c' substitute flag in a global subcommand!");
			}
			return new SubstitutionOperation(definition);
		}
		else if(command.startsWith("d")) {
			return DeleteOperation.INSTANCE;
		}
		else if(command.startsWith("y")) {
			if (command.length() >= 3) {
				return new YankOperation(command.substring(command.length() - 1));
			} else {
				return YankOperation.INSTANCE;
			}
		}

		return null;
	}

	private void executeExCommand(LineRange lineRange, boolean findMatch,
			Pattern regex, LineWiseOperation operation, EditorAdaptor editorAdaptor) {

		/** Starting line, inclusive. */
		int startLine = lineRange.getStartLine();
		/** Ending line, inclusive. */
		int endLine = lineRange.getEndLine();
		TextContent modelContent = editorAdaptor.getModelContent();
		LineInformation line = modelContent.getLineInformation(startLine);

		editorAdaptor.getHistory().beginCompoundChange();
		editorAdaptor.getHistory().lock("ex-command");
		try {
			if (startLine == endLine) {
				processLine(regex, findMatch, operation, line, editorAdaptor);
			}
			else {
				processMultipleLines(regex, findMatch, operation, line, editorAdaptor, startLine,
						endLine, modelContent);
			}
		} finally {
			editorAdaptor.getHistory().unlock("ex-command");
			editorAdaptor.getHistory().endCompoundChange();
		}
	}

	private void processMultipleLines(Pattern regex, boolean findMatch,
			LineWiseOperation operation, LineInformation line, EditorAdaptor editorAdaptor, 
			int startLine, int endLine, TextContent modelContent) {
		int linesProcessed = 0;
		int nLines = modelContent.getNumberOfLines();
		// Hard limit ( + 1 because endLine is inclusive )
		int maxLinesToProcess = endLine - startLine + 1;
		CursorService cs = editorAdaptor.getCursorService();

		while (linesProcessed < maxLinesToProcess && line != null) {
			nLines = modelContent.getNumberOfLines();
			if (nLines > line.getNumber() + 1) {
				LineInformation nextLine = modelContent.getLineInformation(line.getNumber() + 1);
				cs.setMark(NEXTLINE_MARK, cs.newPositionForModelOffset(nextLine.getBeginOffset()));
			} else {
				// Remove mark for exit condition
				cs.deleteMark(NEXTLINE_MARK);
			}
			processLine(regex, findMatch, operation, line, editorAdaptor);
			Position nextLineStart = cs.getMark(NEXTLINE_MARK);
			// Try to guess our position if user ran something like 2d and removed next line
			int updatedNLines = modelContent.getNumberOfLines();
			if (nextLineStart == null && nLines > updatedNLines) {
				// Do nothing except check end of document - "line" object should stay the same if still in bounds
				if (line.getNumber() >= updatedNLines) {
					line = null;
				}
			} else if (nextLineStart == null) {
				// Either we ran out of lines or somebody replaced the contents of the next
				// line (and the mark) without the file getting shorter
				if (line.getNumber() + 1 >= updatedNLines) {
					line = null;
				} else {
					// Assume that line + 1 is our target.
					line = modelContent.getLineInformation(line.getNumber() + 1);
				}
			} else {
				line = modelContent.getLineInformationOfOffset(nextLineStart.getModelOffset());
			}
			linesProcessed++;
		}
		cs.deleteMark(NEXTLINE_MARK);
	}

	private boolean processLine(Pattern regex, boolean findMatch, LineWiseOperation operation,
			LineInformation line, EditorAdaptor editorAdaptor) {
		boolean operationPerformed = false;
		String text = editorAdaptor.getModelContent().getText(line.getBeginOffset(), line.getLength());

		boolean matches = regex.matcher(text).find();
		if( (findMatch && matches) || (!findMatch && !matches) ) {
			try {
				LineRange singleLine = SimpleLineRange.singleLineInModel(editorAdaptor, line);
				operation.execute(editorAdaptor, singleLine);
				operationPerformed = true;
			} catch (CommandExecutionException e) {
			}
		}
		return operationPerformed;
	}

}
