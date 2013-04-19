package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineAddressParser;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
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
	
	private String address;
	private boolean move; //move (true) or copy (false)
	
	public CopyMoveLinesOperation(String address, boolean move) {
		//address should be something like "co 12" or "co .+3"
		//chop off the operation and space, leaving the definition
    	this.address = address.substring(address.indexOf(' ')).trim();
    	this.move = move;
	}

	@Override
	public TextOperation repetition() {
		return this;
	}

	@Override 
	public void execute(EditorAdaptor editorAdaptor, TextRange region,
			ContentType contentType) throws CommandExecutionException {
		
		Position destination = LineAddressParser.parseAddressPosition(address, editorAdaptor);
		
		if(destination == null) {
			throw new CommandExecutionException("Invalid address");
		}
		
		TextContent content = editorAdaptor.getModelContent();
		String lines = content.getText(region);
		//destination might be a position in the middle of a line
		LineInformation line = content.getLineInformationOfOffset(destination.getModelOffset());
		int offset = content.getLineInformation(line.getNumber() + 1).getBeginOffset();
		
		editorAdaptor.getHistory().beginCompoundChange();
		content.replace(offset, 0, lines);
    		
		if(move) {
			//remove the old content
			content.replace(region.getStart().getModelOffset(), region.getModelLength(), "");
		}
		
		editorAdaptor.getHistory().endCompoundChange();
	}

}
