package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineAddressParser;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

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
	
	private String address = null;
	private boolean move; //move (true) or copy (false)
	
	public CopyMoveLinesOperation(String op, boolean move) {
		// Find the end of the operation -- first non-alpha and non-whitespace
	    // character.
	    assert op.length() >= 1;
		int addrPos;
		for (addrPos = 1; addrPos < op.length(); ++addrPos) {
		    final char ch = op.charAt(addrPos);
		    if (!Character.isAlphabetic(ch) && !Character.isWhitespace(ch)) {
		        break;
		    }
		}
		//chop off the operation and an optional space, leaving the definition
		if (addrPos < op.length()) {
		    this.address = op.substring(addrPos).trim();
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
		
		if(address == null) {
			throw new CommandExecutionException("Address required");
		}

		Position destination = LineAddressParser.parseAddressPosition(address, editorAdaptor);
		
		if(destination == null) {
			throw new CommandExecutionException("Invalid address");
		}
		
		TextContent content = editorAdaptor.getModelContent();
		String lines = content.getText(region);
		//destination might be a position in the middle of a line
		LineInformation line = content.getLineInformationOfOffset(destination.getModelOffset());
		int offset = content.getLineInformation(line.getNumber() + 1).getBeginOffset();
		final int sourceOffset = region.getStart().getModelOffset();
		final LineInformation sourceLine = content.getLineInformationOfOffset(region.getLeftBound().getModelOffset());
		final int cursorPos = offset + VimUtils.getFirstNonWhiteSpaceOffset(content, sourceLine) - sourceLine.getBeginOffset();
		
		editorAdaptor.getHistory().beginCompoundChange();
		//
		// Make sure to perform delete/insert operation in reverse order
		// of offsets to guarantee both offsets are valid.
		//
		if (move && offset < sourceOffset) {
		    content.replace(sourceOffset, region.getModelLength(), "");
		}
		content.replace(offset, 0, lines);
		editorAdaptor.setPosition(
		        editorAdaptor.getCursorService().newPositionForModelOffset(cursorPos),
		        StickyColumnPolicy.ON_CHANGE);
		if (move && offset >= sourceOffset) {
		    content.replace(sourceOffset, region.getModelLength(), "");
		}
		
		editorAdaptor.getHistory().endCompoundChange();
	}

}
