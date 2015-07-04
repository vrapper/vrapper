package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Takes a user-defined String such as:
 * :g/^a/normal wdw
 * :g/something/s/foo/bar/g
 * And parses all the pieces.  The expected pieces are:
 * 'g', 'g!', or 'v' to determine whether we find matches or non-matches
 * /{pattern}/ to determine what we're matching on
 * command name (e.g., 'normal', 's', 'd', etc.)
 * any command args (e.g., 'wdw' or '/foo/bar/g')
 *
 * We take those pieces and generate a Command if everything is valid.
 */
public class ExCommandOperation extends SimpleTextOperation {

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

	public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType)
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

		if (definition.length() <= patternEnd + 1) {
			// pattern was defined, but no command
			throw new CommandExecutionException("No ex command to execute on pattern match!");
		}

		//chop off pattern (+delimiter), all that should be left is command
		definition = definition.substring(patternEnd + 1);

		SimpleTextOperation operation = buildExCommand(definition, editorAdaptor);

		if(operation != null) {
			executeExCommand(region, findMatch, pattern, operation, editorAdaptor);
		}
	}

	private SimpleTextOperation buildExCommand(String command, EditorAdaptor editorAdaptor) {
		if(command.startsWith("normal ")) {
			String args = command.substring("normal ".length());
			return new AnonymousMacroOperation(args);
		}
		else if(command.startsWith("s")) {
			return new SubstitutionOperation(command);
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

	private void executeExCommand(TextRange region, boolean findMatch,
			String pattern, SimpleTextOperation operation, EditorAdaptor editorAdaptor) {

		LineInformation line;
		/** Starting line, inclusive. */
		int startLine;
		/** Ending line, exclusive (inclusive if last line is not empty). */
		int endLine;
		TextContent modelContent = editorAdaptor.getModelContent();
		if (region == null) { //default case, entire file
			line = modelContent.getLineInformation(0);
			startLine = 0;
			endLine = modelContent.getNumberOfLines() - 1;
		}
		else {
			line = modelContent.getLineInformationOfOffset(region.getLeftBound().getModelOffset());
			startLine = line.getNumber();
			endLine = modelContent
					.getLineInformationOfOffset( region.getRightBound().getModelOffset() ).getNumber();
		}

		editorAdaptor.getHistory().beginCompoundChange();
		editorAdaptor.getHistory().lock("ex-command");
		try {
			if (startLine == endLine) {
				processLine(pattern, findMatch, operation, line, editorAdaptor);
			}
			else {
				processMultipleLines(findMatch, pattern, operation, editorAdaptor, line, startLine,
						endLine, modelContent);
			}
		} finally {
			editorAdaptor.getHistory().unlock("ex-command");
			editorAdaptor.getHistory().endCompoundChange();
		}

	}

	private void processMultipleLines(boolean findMatch, String pattern,
			SimpleTextOperation operation, EditorAdaptor editorAdaptor, LineInformation line,
			int startLine, int endLine, TextContent modelContent) {
		int linesProcessed = 0;
		int nLines = modelContent.getNumberOfLines();
		// Hard limit
		int maxLinesToProcess = endLine - startLine;
		if (nLines - 1 == endLine && modelContent.getLineInformation(endLine).getLength() > 0) {
			// Using range and last line is not be empty, include it in processing
			maxLinesToProcess++;
		}
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
			processLine(pattern, findMatch, operation, line, editorAdaptor);
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
	}

	private boolean processLine(String pattern, boolean findMatch, SimpleTextOperation operation,
			LineInformation line, EditorAdaptor editorAdaptor) {
		boolean operationPerformed = false;
		String text = editorAdaptor.getModelContent().getText(line.getBeginOffset(), line.getLength());
		//Java's matches() method expects to match the entire string.
		//If the user isn't explicitly matching beginning or end of
		//a String, fake it out so Java is happy.
		if( ! pattern.startsWith("^")) {
			//line can start with anything
			pattern = ".*" + pattern;
		}
		if( ! pattern.endsWith("$")) {
			//line can end with anything
			pattern += ".*";
		}

		boolean matches = text.matches(pattern);
		if( (findMatch && matches) || (!findMatch && !matches) ) {
			try {
				Position start = editorAdaptor.getCursorService().newPositionForModelOffset(line.getBeginOffset());
				Position end = editorAdaptor.getCursorService().newPositionForModelOffset(line.getEndOffset());
				TextRange range = new StartEndTextRange(start, end);

				operation.execute(editorAdaptor, range, ContentType.LINES);
				operationPerformed = true;
			} catch (CommandExecutionException e) {
			}
		}
		return operationPerformed;
	}

}
