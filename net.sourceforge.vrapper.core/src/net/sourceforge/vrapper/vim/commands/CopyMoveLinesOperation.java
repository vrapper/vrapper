package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * :[range]co[py] {address}				*:co* *:copy*
 *          Copy the lines given by [range] to below the line
 *          given by {address}.
 *
 *                                      *:t*
 * :t			Synonym for copy.
 *
 * :[range]m[ove] {address}			*:m* *:mo* *:move* *E134*
 *          Move the lines given by [range] to below the line
 *          given by {address}.
 */
public class CopyMoveLinesOperation extends SimpleTextOperation {
	
	private int destination;
	private boolean move; //move (true) or copy (false)
    private Pattern endsWithNumber = Pattern.compile("^\\D+?(\\d+)$");
	
	public CopyMoveLinesOperation(int destination, boolean move) {
		this.destination = destination;
		this.move = move;
	}
	
	public CopyMoveLinesOperation(String destination, boolean move) {
    	Matcher match = endsWithNumber.matcher(destination);
    	
    	if(match.matches()) {
			this.destination = Integer.parseInt(match.group(1));
    	}
    	else {
    		this.destination = -1;
    	}
    	this.move = move;
	}

	@Override
	public TextOperation repetition() {
		return this;
	}

	@Override 
	public void execute(EditorAdaptor editorAdaptor, TextRange region,
			ContentType contentType) throws CommandExecutionException {
		
		if(destination < 1) {
			return;
		}
		
		TextContent content = editorAdaptor.getModelContent();
		String lines = content.getText(region);
		int length = region.getModelLength();
		
		if(destination >= content.getNumberOfLines()) {
			destination = content.getNumberOfLines();
		}
		LineInformation destLine = content.getLineInformation(destination);
		
		editorAdaptor.getHistory().beginCompoundChange();
		content.replace(destLine.getBeginOffset(), 0, lines);
    		
		if(move) {
			content.replace(region.getStart().getModelOffset(), length, "");
		}
		editorAdaptor.getHistory().endCompoundChange();
	}

}
