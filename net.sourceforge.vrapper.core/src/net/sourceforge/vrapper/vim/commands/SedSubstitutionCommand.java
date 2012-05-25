package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Perform a substitution on the current line or all lines.
 * For example, :s/foo/blah/g or :%s/foo/blah/g
 */
public class SedSubstitutionCommand extends SimpleRepeatableCommand {
	
	private String substitution;
	private boolean currentLineOnly;
	
	public SedSubstitutionCommand(String substitution, boolean currentLineOnly) {
		this.substitution = substitution;
		this.currentLineOnly = currentLineOnly;
	}

	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
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
		if(currentLineOnly) {
			int offset = editorAdaptor.getPosition().getModelOffset();
			LineInformation currentLine = editorAdaptor.getModelContent().getLineInformationOfOffset(offset);
			//begin and end compound change so a single 'u' undoes all replaces
			editorAdaptor.getHistory().beginCompoundChange();
			success = searchAndReplace.replace(currentLine, find, replace, flags);
			editorAdaptor.getHistory().endCompoundChange();
		}
		else {
			success = false;
			int numLines = editorAdaptor.getModelContent().getNumberOfLines();
			LineInformation line;
			
			//perform search individually on each line in the file
			editorAdaptor.getHistory().beginCompoundChange();
			for(int i=0; i < numLines; i++) {
				line = editorAdaptor.getModelContent().getLineInformation(i);
				success = searchAndReplace.replace(line, find, replace, flags) || success;
			}
			editorAdaptor.getHistory().endCompoundChange();
		}
		
		
		if(! success) {
			editorAdaptor.getUserInterfaceService().setErrorMessage("'"+find+"' not found");
		}
	}

	public Command repetition() {
		return this;
	}

}
