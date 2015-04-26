package net.sourceforge.vrapper.vim.commands;

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

    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
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
			return;
		}
		
		//a search pattern must be defined but it doesn't have to be '/'
		//whatever character is after 'g', 'g!', or 'v' must be pattern delimiter
		char delimiter = definition.charAt(0);
		
		//delimiter is at 1, where is it's match?
		int patternEnd = definition.indexOf(delimiter, 1);
		if(patternEnd == -1) {
			//pattern didn't end
			return;
		}
		
		//grab text between delimiters
		String pattern = definition.substring(1, patternEnd);
		
		if (pattern.length() == 0) {
			// if no pattern defined, use last search
			pattern = editorAdaptor.getRegisterManager().getRegister("/").getContent().getText();
			if (pattern.length() == 0) {
				return;
			}
		}

		if (definition.length() <= patternEnd) {
			// pattern was defined, but no command
			return;
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
		
    	int startLine;
    	int endLine;
    	if(region == null) { //default case, entire file
    		startLine = 0;
    		endLine = editorAdaptor.getModelContent().getNumberOfLines();
    	}
    	else {
	    	startLine = editorAdaptor.getModelContent()
	    			.getLineInformationOfOffset( region.getLeftBound().getModelOffset() ).getNumber();
	    	endLine = editorAdaptor.getModelContent()
	    			.getLineInformationOfOffset( region.getRightBound().getModelOffset() ).getNumber();
    	}
		
		LineInformation line;
		editorAdaptor.getHistory().beginCompoundChange();
		editorAdaptor.getHistory().lock("ex-command");
		if(startLine == endLine) {
			line = editorAdaptor.getModelContent().getLineInformation(startLine);
			processLine(pattern, findMatch, operation, line, editorAdaptor);
		}
		else {
			int oldLineCount = editorAdaptor.getModelContent().getNumberOfLines();
			for(int i=startLine; i < endLine; i++) {
				line = editorAdaptor.getModelContent().getLineInformation(i);
				boolean operationPerformed = processLine(pattern, findMatch, operation, line, editorAdaptor);
				
				if(operationPerformed) {
					int currentNumLines = editorAdaptor.getModelContent().getNumberOfLines();
					//if this was a destructive operation and a line was removed
					//stay in sync
					if(oldLineCount > currentNumLines) {
						//next line moved up, make sure we don't skip it
						i--;
						//make sure we don't run outside our boundary
						endLine--;
						oldLineCount = currentNumLines;
					}
				}
			}
		}
		editorAdaptor.getHistory().unlock("ex-command");
		editorAdaptor.getHistory().endCompoundChange();
		
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
