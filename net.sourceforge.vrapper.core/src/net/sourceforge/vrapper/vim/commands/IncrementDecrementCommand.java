package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class IncrementDecrementCommand extends CountAwareCommand {
	
	public static final IncrementDecrementCommand INCREMENT = new IncrementDecrementCommand(true);
	public static final IncrementDecrementCommand DECREMENT = new IncrementDecrementCommand(false);
	
	private boolean increment;
	
	private IncrementDecrementCommand(boolean increment) {
		this.increment = increment;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		if (count == NO_COUNT_GIVEN) { 
			count = 1; 
		} 
		
		Position cursor = editorAdaptor.getCursorService().getPosition();
		TextContent content = editorAdaptor.getModelContent(); 
	 	LineInformation line = content.getLineInformationOfOffset(cursor.getModelOffset()); 
	 	int cursorIndex = cursor.getModelOffset() - line.getBeginOffset();
	 	String text = content.getText(line.getBeginOffset(), line.getLength()); 
	 	
	 	int numEndIndex = findNumEndBoundary(text, cursorIndex);
	 	if(numEndIndex == -1) {
	 		//no numbers found, bail
	 		return;
	 	}
	 	int numStartIndex = findNumStartBoundary(text, numEndIndex - 1);
	 	
	 	String numStr = text.substring(numStartIndex, numEndIndex);
	 	int numVal;
	 	try {
	 		numVal = Integer.parseInt(numStr);
	 	}
	 	catch(NumberFormatException e) {
	 		//it wasn't a number after all
	 		return;
	 	}
	 	
	 	if(increment) {
	 		numVal += count;
	 	}
	 	else {
	 		numVal -= count;
	 	}
	 	//convert back to string
	 	numStr = ""+numVal;
	 	
	 	content.replace(line.getBeginOffset() + numStartIndex, numEndIndex - numStartIndex, numStr);
	 	
	 	//move cursor to the end of the new number
	 	Position newPos = editorAdaptor.getCursorService().newPositionForModelOffset(
	 			line.getBeginOffset() + numStartIndex + numStr.length() -1
		);
	 	editorAdaptor.getCursorService().setPosition(newPos, true);
	}
	
	/**
	 * Starting at startIndex, find the first number. Then, find the end of that
	 * first number (first non-numeric character after the number).  Return the
	 * index of the end of that number.
	 */
	private int findNumEndBoundary(String text, int startIndex) {
		boolean foundNumber = false;
		int offset = -1;
		for(int i=startIndex; i < text.length(); i++) {
			if(! foundNumber && !Character.isDigit(text.charAt(i))) {
				//still haven't found a number, keep looping
				continue;
			}
			else if(foundNumber && !Character.isDigit(text.charAt(i))) {
				//found the number boundary, this is what we want
				break;
			}
			else { //isDigit == true
				foundNumber = true;
				offset = i;
			}
		}
		
		return offset + 1;
	}
	
	/**
	 * Starting with number character at startIndex, move backwards until we find
	 * where this number started.  Return the index where the number starts.
	 */
	private int findNumStartBoundary(String text, int startIndex) {
		int offset = 0;
		for(int i=startIndex; i >=0; i--) {
			if(Character.isDigit(text.charAt(i))) {
				offset = i;
			}
			else if(text.charAt(i) == '-') {
				//negative number
				offset = i;
				break;
			}
			else {
				//found the number boundary
				break;
			}
		}
		
		return offset;
	}

	@Override
	public CountAwareCommand repetition() {
		return this;
	}

}
