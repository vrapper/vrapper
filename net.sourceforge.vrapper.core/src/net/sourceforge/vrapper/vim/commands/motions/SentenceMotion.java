package net.sourceforge.vrapper.vim.commands.motions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Move cursor forward or backwards to nearest sentence boundary.
 * That is, find closest '.', '!', or '?' then move cursor to first
 * character after any spaces after that sentence character.  Sentence
 * character may also be followed by ')', ']', '"', or ''' before spaces.
 * 
 * For example:
 * This is a sentence.  Here is the cursor.
 *                      ^cursor
 * (This is a sentence.)  Here is the cursor.
 *                        ^cursor
 */
public class SentenceMotion extends CountAwareMotion {
    public static final SentenceMotion FORWARD = new SentenceMotion(true);
    public static final SentenceMotion BACKWARD = new SentenceMotion(false);

    //. ? or ! followed by an optional ) ] " or '
    //followed by a number of spaces, followed by any non-whitespace char
    //(grouping is on that non-whitespace char so we can get its index)
    private static final Pattern pattern = Pattern.compile(".+?[.?!][)\\]\"']*\\s+(\\S).+?");
    
    private boolean forward;
    
    private SentenceMotion(boolean forward) {
    	this.forward = forward;
    }

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if(count == NO_COUNT_GIVEN)
            count = 1;
        
        int position = editorAdaptor.getPosition().getModelOffset();
        for (int i = 0; i < count; i++) {
        	position = doIt(editorAdaptor, position);
        }
        
        return editorAdaptor.getPosition().setModelOffset(position);
	}
	
	private int doIt(EditorAdaptor editorAdaptor, int position) {
        TextContent modelContent = editorAdaptor.getModelContent();
        
        LineInformation line = modelContent.getLineInformationOfOffset(position);
        LineInformation lineTmp;
        int posTmp;
        int offset = getSentenceBoundaryOffset(line, position, modelContent);
        
        while(offset == -1) {
        	if(forward) {
        		if(modelContent.getNumberOfLines() > (line.getNumber() + 1)) {
        			lineTmp = line;
        			//get next line, starting at beginning of line
        			line = modelContent.getLineInformation(line.getNumber() + 1);
        			position = line.getBeginOffset();
        			//empty lines are sentence boundaries too
        			if(line.getRegionLength() == 0 && lineTmp.getRegionLength() != 0 ||
        				line.getRegionLength() != 0 && lineTmp.getRegionLength() == 0) {
        				return line.getBeginOffset();
        			}
        		}
        		else {
        			//already on last line in file, move cursor to the end
        			return line.isEmpty() ? line.getBeginOffset() : line.getEndOffset();
        		}
        	}
        	else { //backwards
        		if(line.getNumber() > 0) {
        			lineTmp = line;
        			posTmp = position;
        			//get previous line, starting at end of line
        			line = modelContent.getLineInformation(line.getNumber() - 1);
        			position = line.getEndOffset();
        			//empty lines are sentence boundaries too
        			if(line.getRegionLength() == 0 && lineTmp.getRegionLength() != 0 ||
        				line.getRegionLength() != 0 && lineTmp.getRegionLength() == 0) {
        				//if posTmp was already at the beginning of this line, go to previous line
        				//otherwise, go to beginning of this line
        				return posTmp == lineTmp.getBeginOffset() ? line.getBeginOffset() : lineTmp.getBeginOffset();
        			}
        		}
        		else {
        			//already on first line in file, move cursor to beginning
        			return 0;
        		}
        	}
        	
        	//check this new line for a sentence
        	offset = getSentenceBoundaryOffset(line, position, modelContent);
        }
        
        return offset;
	}
	
	private int getSentenceBoundaryOffset(LineInformation line, int position, TextContent modelContent) {
        int begin = line.getBeginOffset();
        String text;
        if(forward) {
        	int length = line.getRegionLength() - (position - begin);
        	//start at cursor, get text to end of line
        	text = modelContent.getText(position, length);
        }
        else {
        	//start at beginning of line, get text to cursor
        	text = modelContent.getText(begin, position - begin);
        }
        
        //collect start index of each match
        List<Integer> matches = new ArrayList<Integer>();
        Matcher match = pattern.matcher(text);
        while(match.find()) { matches.add(match.start(1)); }
        
        if(matches.size() > 0) {
        	//first match if forward, last match if backwards
        	//(match offsets are local to this line, convert to modelContent offset)
        	return forward ? matches.get(0) + position : matches.get( matches.size() -1 ) + begin;
        }
        else { //no sentence boundary found
        	return -1;
        }
	}

	public BorderPolicy borderPolicy() {
		return BorderPolicy.EXCLUSIVE;
	}

	public boolean updateStickyColumn() {
		return true;
	}
}
