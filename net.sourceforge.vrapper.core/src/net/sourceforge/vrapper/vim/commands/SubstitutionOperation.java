package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Perform a substitution on a range of lines.  Can be current line,
 * all lines, or any range in between.
 * For example, :s/foo/blah/g or :%s/foo/blah/g or :2,5s/foo/blah/g
 */
public class SubstitutionOperation extends AbstractLinewiseOperation {

	private SubstitutionDefinition subDef;

	public SubstitutionOperation(SubstitutionDefinition substitution) {
		this.subDef = substitution;
	}

	public SubstitutionDefinition getDefinition() {
		return subDef;
	}

    @Override
    public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
            throws CommandExecutionException {
        return SimpleLineRange.singleLine(editorAdaptor, currentPos);
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, LineRange range) throws CommandExecutionException {
        TextContent model = editorAdaptor.getModelContent();
		int numReplaces = 0;
		int lineReplaceCount = 0;
		if (range.getStartLine() == range.getEndLine()) {
			LineInformation currentLine = model.getLineInformation(range.getStartLine());
			//begin and end compound change so a single 'u' undoes all replaces
			editorAdaptor.getHistory().beginCompoundChange();
			numReplaces = performReplace(currentLine, subDef.find, subDef.replace, subDef.flags, editorAdaptor);
			editorAdaptor.getHistory().endCompoundChange();
		}
		else {
			LineInformation line;
			int lineChanges = 0;

			int endLine = range.getEndLine();
			int totalLines = model.getNumberOfLines();
			int lineDiff;
			//perform search individually on each line in the range
			//(so :%s without 'g' flag runs once on each line)
			editorAdaptor.getHistory().beginCompoundChange();
			for(int i=range.getStartLine(); i <= endLine; i++) {
				line = model.getLineInformation(i);
				lineChanges = performReplace(line, subDef.find, subDef.replace, subDef.flags, editorAdaptor);
				if(lineChanges > 0) {
					lineReplaceCount++;
				}
				numReplaces += lineChanges;

				lineDiff = model.getNumberOfLines() - totalLines;
				if(lineDiff > 0) {
				    //lines were introduced as a result of this replacement
				    //skip over those introduced lines and move on to the next intended line
				    i += lineDiff;
				    endLine += lineDiff;
				    totalLines += lineDiff;
				}
			}
			editorAdaptor.getHistory().endCompoundChange();
		}
		
		if (numReplaces == 0) {
			editorAdaptor.getUserInterfaceService().setErrorMessage("'"+subDef.find+"' not found");
		} else {
			String message = numReplaces + " ";
			message += subDef.flags.contains("n") ? "matches" : "substitutions";
			if (lineReplaceCount > 1) {
				message += " on " + lineReplaceCount + " lines";
			} else {
				message += " on 1 line";
			}
			editorAdaptor.getUserInterfaceService().setInfoMessage(message);
		}
		
		//enable '&', 'g&', and ':s' features
		// [TODO] Move to substitution parser
		editorAdaptor.getRegisterManager().setLastSubstitution(this);
	}
    
    private int performReplace(LineInformation line, String find,
    		String replace, String flags, EditorAdaptor editorAdaptor) {
    	//Eclipse regex doesn't handle '^' and '$' like Vim does.
    	//Time for some special cases!
		if(find.equals("^")) {
			replace = convertToPlatformNewline(editorAdaptor, replace);
			// insert the text at the beginning of the line
			editorAdaptor.getModelContent().replace(line.getBeginOffset(), 0, replace);
			return 1;
		}
		else if(find.equals("$")) {
			replace = convertToPlatformNewline(editorAdaptor, replace);
			// insert the text at the end of the line
			editorAdaptor.getModelContent().replace(line.getEndOffset(), 0, replace);
			return 1;
		}
		else {
		    int start = line.getBeginOffset();
		    int end = line.getEndOffset();
		    if(find.contains("\\%V")) { //select only within visual area (not lines)
		        find = find.replaceAll("\\\\%V", "");
		        Selection selection = editorAdaptor.getSelection();
		        if(selection != null) {
		            start = selection.getLeftBound().getModelOffset();
		            end = selection.getRightBound().getModelOffset();
		        }
		    }
			//let Eclipse handle the regex
			SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
			return searchAndReplace.replace(start, end, find, replace, flags);
		}
    }

	/**
	 * Replace Eclipse's platform independent newline ("\R") with a platform
	 * specific newline.
	 * 
	 * This is useful when a string needs to be used by non-Eclipse code.
	 */
	private String convertToPlatformNewline(EditorAdaptor editorAdaptor, String inputString) {
		String platformNewline = editorAdaptor.getConfiguration().getNewLine();
		/*
		 * Change "\R" to platform newline only if it is preceded by zero or an even number of
		 * backslashes.
		 */
		String result = inputString.replaceAll("((^|[^\\\\])(\\\\\\\\)*)(\\\\R)", "$1" + platformNewline);
		return result;
	}

	public TextOperation repetition() {
		return this;
	}

}
