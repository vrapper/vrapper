package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Perform a substitution on the current line or all lines.
 * For example, :s/foo/blah/g or :%s/foo/blah/g
 */
public class SedSubstitutionOperation extends SimpleTextOperation {
	
	private String substitution;
	
	public SedSubstitutionOperation(String substitution) {
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
		//'s' or '%s' = pieces[0]
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
		SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
		if(startLine == endLine) {
			LineInformation currentLine = editorAdaptor.getModelContent().getLineInformation(startLine);
			//begin and end compound change so a single 'u' undoes all replaces
			editorAdaptor.getHistory().beginCompoundChange();
			success = searchAndReplace.replace(currentLine, find, replace, flags);
			editorAdaptor.getHistory().endCompoundChange();
		}
		else {
			success = false;
			LineInformation line;
			
			//perform search individually on each line in the range
			editorAdaptor.getHistory().beginCompoundChange();
			for(int i=startLine; i <= endLine; i++) {
				line = editorAdaptor.getModelContent().getLineInformation(i);
				success = searchAndReplace.replace(line, find, replace, flags) || success;
			}
			editorAdaptor.getHistory().endCompoundChange();
		}
		
		
		if(! success) {
			editorAdaptor.getUserInterfaceService().setErrorMessage("'"+find+"' not found");
		}
	}

	public TextOperation repetition() {
		return this;
	}

}
