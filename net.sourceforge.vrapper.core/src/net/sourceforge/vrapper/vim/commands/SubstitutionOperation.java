package net.sourceforge.vrapper.vim.commands;

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
		
		boolean success;
		if(startLine == endLine) {
			LineInformation currentLine = editorAdaptor.getModelContent().getLineInformation(startLine);
			//begin and end compound change so a single 'u' undoes all replaces
			editorAdaptor.getHistory().beginCompoundChange();
			success = performReplace(currentLine, find, replace, flags, editorAdaptor);
			editorAdaptor.getHistory().endCompoundChange();
		}
		else {
			success = false;
			LineInformation line;
			
			//perform search individually on each line in the range
			//(so :%s without 'g' flag runs once on each line)
			editorAdaptor.getHistory().beginCompoundChange();
			for(int i=startLine; i < endLine; i++) {
				line = editorAdaptor.getModelContent().getLineInformation(i);
				success = performReplace(line, find, replace, flags, editorAdaptor) || success;
			}
			editorAdaptor.getHistory().endCompoundChange();
		}
		
		if(! success) {
			editorAdaptor.getUserInterfaceService().setErrorMessage("'"+find+"' not found");
		}
		
		//enable '&', 'g&', and ':s' features
		editorAdaptor.getRegisterManager().setLastSubstitution(this);
	}
    
    private boolean performReplace(LineInformation line, String find,
    		String replace, String flags, EditorAdaptor editorAdaptor) {
    	//Eclipse regex doesn't handle '^' and '$' like Vim does.
    	//Time for some special cases!
		if(find.equals("^")) {
			//insert the text at the beginning of the line
            editorAdaptor.getModelContent().replace(line.getBeginOffset(), 0, replace);
			return true;
		}
		else if(find.equals("$")) {
			//insert the text at the end of the line
            editorAdaptor.getModelContent().replace(line.getEndOffset(), 0, replace);
			return true;
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
