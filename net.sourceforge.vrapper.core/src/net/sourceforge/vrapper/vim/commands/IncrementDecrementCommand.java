package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Increment (Ctrl-A) or decrement (Ctrl-X) the number under (or to the right of)
 * the cursor.  Supports decimals (123), octals (0123), and hex (0x123).
 * Decimals will remove leading zeroes (0099), octals and hex will preserve
 * leading zeroes (0023), (0x00af).
 */
public class IncrementDecrementCommand extends CountAwareCommand {
	
	public static final IncrementDecrementCommand INCREMENT = new IncrementDecrementCommand(true);
	public static final IncrementDecrementCommand DECREMENT = new IncrementDecrementCommand(false);
	
	private boolean increment;
	
	private IncrementDecrementCommand(boolean increment) {
		this.increment = increment;
	}
	
	//utility class, basically a struct
	private class NumBoundary {
		int numStartIndex;
		int numEndIndex;
		int radix;
		
		public NumBoundary(int numStartIndex, int numEndIndex, int radix) {
			this.numStartIndex = numStartIndex;
			this.numEndIndex = numEndIndex;
			this.radix = radix;
		}
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
	 	NumBoundary boundary = null;
	 	
	 	//Look for both hex and integers (decimal and octal).
	 	//Hex is a superset of the integer characters
	 	//so it'll have a different boundary.
	 	NumBoundary hexBoundary = findHex(text, cursorIndex);
	 	NumBoundary numBoundary = findIntegers(text, cursorIndex);
	 	
	 	//if we found hex, but integers appear first (and aren't the '0' in '0x')
	 	//then use the integer boundary instead
	 	//(e.g., 'xxx123xxx0x456' should increment '123', not '0x456')
	 	if(numBoundary == null ||
	 			(hexBoundary != null && (numBoundary.numStartIndex >= hexBoundary.numStartIndex ||
	 			numBoundary.numStartIndex == hexBoundary.numStartIndex - 2))) {
	 		boundary = hexBoundary;
	 	}
	 	else {
	 		boundary = numBoundary;
	 	}
	 	
	 	String numStr;
	 	if(boundary == null) {
	 		//no numbers found to increment, bail
	 		return;
	 	}
	 	else {
	 		String origNumStr = text.substring(boundary.numStartIndex, boundary.numEndIndex + 1);
	 		numStr = modifyNumber(origNumStr, count, boundary.radix);
	 		if(numStr == null) {
	 			//failed to parse number
	 			return;
	 		}
	 		
	 		//restore any leading 0's that were removed during parseInt
	 		if(boundary.radix == 8) {
	 			while(origNumStr.length() > numStr.length() + 1) {
	 				numStr = '0' + numStr;
	 			}
	 			//octal is defined by always having a leading 0
	 			numStr = '0' + numStr;
	 		}
	 		else if(boundary.radix == 16) {
	 			while(origNumStr.length() > numStr.length()) {
	 				numStr = '0' + numStr;
	 			}
	 			//preserve case of a-f/A-F in original string
	 			//(java always returns lower-case)
	 			if(origNumStr.matches(".*[A-F].*")) {
	 				numStr = numStr.toUpperCase();
	 			}
	 		}
	 		//else, radix = 10 (decimal), no special cases
	 	}
	 	
	 	content.replace(line.getBeginOffset() + boundary.numStartIndex,
	 			boundary.numEndIndex + 1 - boundary.numStartIndex, numStr);
	 	
	 	//move cursor to the end of the new number
	 	Position newPos = editorAdaptor.getCursorService().newPositionForModelOffset(
	 			line.getBeginOffset() + boundary.numStartIndex + numStr.length() -1
		);
	 	editorAdaptor.getCursorService().setPosition(newPos, true);
	}
	
	/**
	 * Starting at cursor position 'index', find the bounds for the closest
	 * hex number directly below or to the right of 'index' (if any).  Return
	 * null if no hex number found.  Hex is defined by a string of hex digits
	 * (0-9,a-f,A-F) preceded by '0x' or '0X'.
	 */
	private NumBoundary findHex(String text, int index) {
	 	if(! text.matches(".*0[xX].*")) {
	 		//'0x' doesn't exist in this line, no hex anywhere
	 		return null;
	 	}

	 	int hexEndIndex = findHexEndBoundary(text, index);
	 	if(hexEndIndex == -1) {
	 		//there's no hex number at (or after) index
	 		//(the hex must be defined and completed before the cursor)
	 		return null;
	 	}
	 	
	 	int hexStartIndex = findHexStartBoundary(text, hexEndIndex);
	 	if(hexStartIndex >= 2 && (text.charAt(hexStartIndex-1) == 'x' || text.charAt(hexStartIndex-1) == 'X')
	 			&& text.charAt(hexStartIndex-2) == '0') {
	 		//we found a string of hex digits preceded by '0x', it's hex
	 		return new NumBoundary(hexStartIndex, hexEndIndex, 16);
	 	}
	 	else if(text.indexOf("0x", index) > -1 || text.indexOf("0X", index) > -1) {
	 		//The 'index' we started with happened to have a hex character under it
	 		//(a-f) but wasn't actually defining a hex number.  Try again, but this
	 		//time start where we know a hex number is being defined.
	 		int newStart = text.indexOf("0x", index); //small 'x'
	 		int otherChoice = text.indexOf("0X", index); //big 'x'
	 		if(otherChoice != -1 && otherChoice < newStart) {
	 			//either '0x' wasn't found or there's a '0X' before '0x'
	 			newStart = otherChoice;
	 		}
	 		return findHex(text, newStart);
	 	}
	 		
	 	//we found a string of hex digits but they didn't
	 	//have a '0x' preceding them, ignore
	 	return null;
	}
	
	/**
	 * Starting at cursor position 'index', find the bounds for the closest
	 * number directly below or to the right of 'index' (if any).  Return
	 * null if no number found.  Handles both decimal and octal.  Octal
	 * is defined by a leading '0' and no digits greater than 7.  All other
	 * sequence of digits are treated as decimal.
	 */
	private NumBoundary findIntegers(String text, int index) {
		int numEndIndex = findNumEndBoundary(text, index);
		if(numEndIndex == -1) {
			//no numbers found, bail
			return null;
		}
		int numStartIndex = findNumStartBoundary(text, numEndIndex);

		String numStr = text.substring(numStartIndex, numEndIndex + 1);
		if(text.charAt(numStartIndex) == '0' && isOctal(numStr)) {
			//octal
			return new NumBoundary(numStartIndex, numEndIndex, 8);
		}
		else {
			//decimal
			return new NumBoundary(numStartIndex, numEndIndex, 10);
		}
	}
	
	/**
	 * Check to see if any of the digits in this string are greater than 7.
	 * If they are, this string can't be treated as octal.
	 */
	private boolean isOctal(String numStr) {
		if(numStr.length() == 1 && numStr.charAt(0) == '0') {
			//the number '0' by itself isn't octal
			return false;
		}
		
		for(int i=0; i < numStr.length(); i++) {
			if(Character.digit(numStr.charAt(i), 8) == -1) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Perform the actual increment (or decrement) on this string.
	 * 
	 * @param numStr - String of either decimal, octal, or hex digits
	 * @param count - number to increment/decrement numStr by
	 * @param radix - is numStr decimal, octal, or hex?
	 * @return - return a String similar to numStr but incremented/decremented by 'count'
	 */
	private String modifyNumber(String numStr, int count, int radix) {
	 	int numVal;
	 	try {
	 		numVal = Integer.parseInt(numStr, radix);
	 	}
	 	catch(NumberFormatException e) {
	 		//it wasn't a number after all
	 		return null;
	 	}
	 	
	 	if(increment) {
	 		numVal += count;
	 	}
	 	else {
	 		numVal -= count;
	 	}
	 	
	 	//convert back to string
	 	return Integer.toString(numVal, radix);
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
			else { //isDigit == true, found a number
				foundNumber = true;
				offset = i;
			}
		}
		
		return offset;
	}
	
	/**
	 * Starting at startIndex, find the first hex number. Then, find the end of that
	 * hex number (first non-hex character after the number).  Return the
	 * index of the end of that hex number.
	 */
	private int findHexEndBoundary(String text, int startIndex) {
		boolean foundHex = false;
		int offset = -1;
		char test;
		for(int i=startIndex; i < text.length(); i++) {
			test = text.charAt(i);
			if(! foundHex && Character.digit(test, 16) == -1) {
				//still haven't found a hex digit, keep looping
				continue;
			}
			else if(foundHex && Character.digit(test, 16) == -1) {
				//found the number boundary, this is what we want
				break;
			}
			else { //is hex
				if(!foundHex && test == '0' && text.length() > i+1 && text.charAt(i+1) == 'x') {
					//skip over 'x', it isn't actually a hex digit
					i++;
				}
				foundHex = true;
				offset = i;
			}
		}
		
		return offset;
	
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
	
	/**
	 * Find the first hex digit after '0x' starting at startIndex.
	 */
	private int findHexStartBoundary(String text, int startIndex) {
		int offset = 0;
		for(int i=startIndex; i >=0; i--) {
			if(Character.digit(text.charAt(i), 16) > -1) {
				offset = i;
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
