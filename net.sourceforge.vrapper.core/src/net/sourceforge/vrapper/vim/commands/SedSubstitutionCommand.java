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
		
		int start;
		int end;
		if(currentLineOnly) {
			int offset = editorAdaptor.getPosition().getModelOffset();
			LineInformation currentLine = editorAdaptor.getModelContent().getLineInformationOfOffset(offset);
			start = currentLine.getBeginOffset();
			end = currentLine.getEndOffset();
		}
		else {
			start = 0;
			end = editorAdaptor.getModelContent().getTextLength();
		}
		
		//begin and end compound change so a single 'u' undoes all replaces
		editorAdaptor.getHistory().beginCompoundChange();
		SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
		boolean success = searchAndReplace.replace(start, end, find, replace, flags);
		editorAdaptor.getHistory().endCompoundChange();
		
		if(! success) {
            throw new CommandExecutionException("'"+find+"' not found");
		}
	}

	public Command repetition() {
		return this;
	}

}
