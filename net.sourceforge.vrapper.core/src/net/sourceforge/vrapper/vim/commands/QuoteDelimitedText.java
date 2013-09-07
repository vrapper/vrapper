package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindQuoteMotion;

public class QuoteDelimitedText implements DelimitedText {
	
    private char delimiter;
	
    public QuoteDelimitedText(char delimiter) {
    	this.delimiter = delimiter;
    }

	public TextRange leftDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		FindQuoteMotion leftMotion = new FindQuoteMotion(delimiter, true);
		int leftOffset = leftMotion.destination(offset, editorAdaptor.getModelContent(), count);
		Position left = editorAdaptor.getCursorService().newPositionForModelOffset(leftOffset);
		return new StartEndTextRange(left, left.addModelOffset(1));
	}

	public TextRange rightDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		//we need left to calculate right
		TextRange left = leftDelimiter(offset, editorAdaptor, count);
		
		//right has to be after left, but left might be after the cursor
        CountAwareMotion rightMotion = new FindQuoteMotion(delimiter, left.getLeftBound().getModelOffset() + 1);
        Position right = rightMotion.destination(editorAdaptor, count);
        return new StartEndTextRange(right, right.addModelOffset(1));
	}

}
