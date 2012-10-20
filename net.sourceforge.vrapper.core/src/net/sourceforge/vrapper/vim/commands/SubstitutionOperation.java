package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

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
    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
    	int startLine;
    	int endLine;
    	if(region == null) {
    		//special case, recalculate 'current line' every time
    		//(this is to ensure '.' always works on current line)
    		int offset = editorAdaptor.getPosition().getModelOffset();
			startLine = editorAdaptor.getModelContent().getLineInformationOfOffset(offset).getNumber();
			endLine = startLine;
    	}
    	else {
	    	startLine = editorAdaptor.getModelContent()
	    			.getLineInformationOfOffset( region.getLeftBound().getModelOffset() ).getNumber();
	    	endLine = editorAdaptor.getModelContent()
	    			.getLineInformationOfOffset( region.getRightBound().getModelOffset() ).getNumber();
    	}
    	
		//whatever character is after 's' is our delimiter
		String delim = "" + substitution.charAt( substitution.indexOf('s') + 1);
		String[] fields = substitution.split(delim);
		String find = "";
		String replace = "";
		String flags = "";
		//'s' or '%s' = fields[0]
		if(fields.length > 1) {
			find = fields[1];
		}
		if(fields.length > 2) {
			replace = fields[2];
		}
		if(fields.length > 3) {
			flags = fields[3];
		}
		
		//before attempting substitution, is this regex even valid?
		try {
	        Pattern.compile(find);
	    } catch (PatternSyntaxException e) {
			editorAdaptor.getUserInterfaceService().setErrorMessage(e.getDescription());
			return;
	    }
		
		int numReplaces = 0;
		int lineReplaceCount = 0;
		if(startLine == endLine) {
			LineInformation currentLine = editorAdaptor.getModelContent().getLineInformation(startLine);
			//begin and end compound change so a single 'u' undoes all replaces
			editorAdaptor.getHistory().beginCompoundChange();
			numReplaces = performReplace(currentLine, find, replace, flags, editorAdaptor);
			editorAdaptor.getHistory().endCompoundChange();
		}
		else {
			LineInformation line;
			int lineChanges = 0;
			
			//perform search individually on each line in the range
			//(so :%s without 'g' flag runs once on each line)
			editorAdaptor.getHistory().beginCompoundChange();
			for(int i=startLine; i < endLine; i++) {
				line = editorAdaptor.getModelContent().getLineInformation(i);
				lineChanges = performReplace(line, find, replace, flags, editorAdaptor);
				if(lineChanges > 0) {
					lineReplaceCount++;
				}
				numReplaces += lineChanges;
			}
			editorAdaptor.getHistory().endCompoundChange();
		}
		
		if(numReplaces == 0) {
			editorAdaptor.getUserInterfaceService().setErrorMessage("'"+find+"' not found");
		}
		else if(lineReplaceCount > 0) {
			editorAdaptor.getUserInterfaceService().setInfoMessage(
					numReplaces + " substitutions on " + lineReplaceCount + " lines"
			);
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
