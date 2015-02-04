package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ConfirmSubstitutionMode;

/**
 * Perform a substitution on a range of lines.  Can be current line,
 * all lines, or any range in between.
 * For example, :s/foo/blah/g or :%s/foo/blah/g or :2,5s/foo/blah/g
 */
public class SubstitutionOperation extends SimpleTextOperation {
	
	private String substitution;
	
	public SubstitutionOperation(String substitution) {
		this.substitution = substitution;
	}

    @Override
    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) throws CommandExecutionException {
        TextContent model = editorAdaptor.getModelContent();
    	int startLine;
    	int endLine;
    	if(region == null) {
    		//special case, recalculate 'current line' every time
    		//(this is to ensure '.' always works on current line)
    		int offset = editorAdaptor.getPosition().getModelOffset();
			startLine = model.getLineInformationOfOffset(offset).getNumber();
			endLine = startLine;
    	}
    	else {
	    	startLine = model.getLineInformationOfOffset( region.getLeftBound().getModelOffset() ).getNumber();
	    	endLine = model.getLineInformationOfOffset( region.getRightBound().getModelOffset() ).getNumber();
	    	if(model.getTextLength() == region.getRightBound().getModelOffset()) {
	    	    //the endLine calculation is off-by-one for the last line in the file
	    	    //force it to actually use the last line
	    	    endLine = model.getNumberOfLines();
	    	}
    	}
    	
    	SubstitutionDefinition subDef;
    	try {
    	    subDef = new SubstitutionDefinition(substitution, editorAdaptor.getRegisterManager());
    	}
    	catch(IllegalArgumentException e) {
			throw new CommandExecutionException(e.getMessage());
    	}
    	
    	if(subDef.flags.indexOf('c') > -1) {
    	    //move into "confirm" mode
    	    editorAdaptor.changeModeSafely(ConfirmSubstitutionMode.NAME, new ConfirmSubstitutionMode.SubstitutionConfirm(subDef, startLine, endLine));
    	    return;
    	}
		
		int numReplaces = 0;
		int lineReplaceCount = 0;
		if(startLine == endLine) {
			LineInformation currentLine = model.getLineInformation(startLine);
			//begin and end compound change so a single 'u' undoes all replaces
			editorAdaptor.getHistory().beginCompoundChange();
			numReplaces = performReplace(currentLine, subDef.find, subDef.replace, subDef.flags, editorAdaptor);
			editorAdaptor.getHistory().endCompoundChange();
		}
		else {
			LineInformation line;
			int lineChanges = 0;
			
			int totalLines = model.getNumberOfLines();
			int lineDiff;
			//perform search individually on each line in the range
			//(so :%s without 'g' flag runs once on each line)
			editorAdaptor.getHistory().beginCompoundChange();
			for(int i=startLine; i < endLine; i++) {
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
		editorAdaptor.getRegisterManager().setLastSubstitution(this);
	}
    
    private int performReplace(LineInformation line, String find,
    		String replace, String flags, EditorAdaptor editorAdaptor) {
    	//Eclipse regex doesn't handle '^' and '$' like Vim does.
    	//Time for some special cases!
		if(find.equals("^")) {
			//insert the text at the beginning of the line
            editorAdaptor.getModelContent().replace(line.getBeginOffset(), 0, replace);
			return 1;
		}
		else if(find.equals("$")) {
			//insert the text at the end of the line
            editorAdaptor.getModelContent().replace(line.getEndOffset(), 0, replace);
			return 1;
		}
		else {
			//let Eclipse handle the regex
			SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
			return searchAndReplace.replace(line, find, replace, flags);
		}
    }

	public TextOperation repetition() {
		return this;
	}

}
