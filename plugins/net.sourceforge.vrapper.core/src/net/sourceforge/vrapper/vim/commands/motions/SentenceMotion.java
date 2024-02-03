package net.sourceforge.vrapper.vim.commands.motions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
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
    public static final SentenceMotion FORWARD = new SentenceMotion(true, false);
    public static final SentenceMotion BACKWARD = new SentenceMotion(false, false);

    //. ? or ! followed by an optional ) ] " or '
    //followed by a number of spaces, followed by any non-whitespace char (or end of string)
    //(grouping is on that non-whitespace char so we can get its index)
    private static final Pattern pattern = Pattern.compile("(?<=[.?!])[)\\]\"']*\\s+(\\S|$)");
    private static final Pattern endOnSentence = Pattern.compile(".*[.?!][)\\]\"']*$");
    
    private boolean forward;
    private boolean includeCursor;
    
    private SentenceMotion(boolean forward, boolean includeCursor) {
    	this.forward = forward;
    	this.includeCursor = includeCursor;
    }

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition) throws CommandExecutionException {
        if(count == NO_COUNT_GIVEN)
            count = 1;

        Position cursor = fromPosition;
        int position = cursor.getModelOffset();
        for (int i = 0; i < count; i++) {
            position = doIt(editorAdaptor, position);
        }

        return cursor.setModelOffset(position);
	}
	
	private int doIt(EditorAdaptor editorAdaptor, int position) {
        TextContent modelContent = editorAdaptor.getModelContent();
        
        LineInformation line = modelContent.getLineInformationOfOffset(position);
        LineInformation lineTmp;
        int posTmp;
        int offset = getSentenceBoundaryOffset(line, position, modelContent, includeCursor);
        
        while(offset == -1) {
        	if(forward) {
        		if(modelContent.getNumberOfLines() > (line.getNumber() + 1)) {
        			lineTmp = line;
        			//get next line, starting at beginning of line
        			line = modelContent.getLineInformation(line.getNumber() + 1);
        			position = line.getBeginOffset();
        			//empty lines are sentence boundaries too
        			if(line.getLength() == 0 && lineTmp.getLength() != 0 ||
        				line.getLength() != 0 && lineTmp.getLength() == 0) {
        				return line.getBeginOffset();
        			}
        			else {
        			    //if this line ends on a sentence boundary, return next line start
        			    String lineText = modelContent.getText(lineTmp.getBeginOffset(), lineTmp.getLength());
        			    if(endOnSentence.matcher(lineText).matches()) {
        			        return line.getBeginOffset();
        			    }
        			}
        		}
        		else {
        			//already on last line in file, move cursor to the end
        			return line.getEndOffset();
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
        			if(line.getLength() == 0 && lineTmp.getLength() != 0) {
        				//if posTmp was already at the beginning of this line, go to previous line
        				//otherwise, go to beginning of this line
        				return posTmp == lineTmp.getBeginOffset() ? line.getBeginOffset() : lineTmp.getBeginOffset();
        			}
        			else {
        			    //if previous line ends on a sentence boundary, return this line start
        			    String lineText = modelContent.getText(line.getBeginOffset(), line.getLength());
        			    if(endOnSentence.matcher(lineText).matches() && posTmp != lineTmp.getBeginOffset()) {
                            //if posTmp was already at the beginning of this line, get next sentence boundary (loop again)
                            //otherwise, go to beginning of this line
        			        return lineTmp.getBeginOffset();
        			    }
        			}
        		}
        		else {
        			//already on first line in file, move cursor to beginning
        			return 0;
        		}
        	}
        	
        	//check this new line for a sentence
        	offset = getSentenceBoundaryOffset(line, position, modelContent, includeCursor);
        }
        
        return offset;
	}

    //includeEnd only applies to text objects.
	//It refers to the end of the string (typically cursor location), not the end of the line.
	private int getSentenceBoundaryOffset(LineInformation line, int position, TextContent modelContent, boolean includeEnd) {
        String text;
        int begin = line.getBeginOffset();

        if(forward) {
        	//start at cursor, get text to end of line
        	text = modelContent.getText(position, line.getEndOffset() - position);
        }
        else {
        	//start at beginning of line, get text to cursor
        	text = modelContent.getText(begin, position - begin);
        }
        
        //collect start index of each match
        List<Integer> matches = new ArrayList<Integer>();
        Matcher match = pattern.matcher(text);
        while(match.find()) {
            //when moving backwards, if the cursor is *on* the beginning of a sentence
            //'(' should jump to the previous sentence, but 'is' should select this sentence.
            
            //if the match is not the end of the string, add it
            //if the match *is* the end of the string *and* we want to include it, add it
            //(includeEnd is only checked if first condition is false, meaning the match *is* the end)
            if(match.start(1) != text.length() || includeEnd) {
                matches.add(match.start(1));
            }
        }
        
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

	public StickyColumnPolicy stickyColumnPolicy() {
		return StickyColumnPolicy.ON_CHANGE;
	}
	
	@Override
	public boolean isJump() {
	    return true;
	}

    public static class SentenceTextObject extends AbstractTextObject {

        private final boolean outer;
        
        public SentenceTextObject(final boolean outer) {
            super();
            this.outer = outer;
        }

        @Override
        public TextRange getRegion(final EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            if (count == NO_COUNT_GIVEN) {
                count = 1;
            }

            final Position cursor = editorAdaptor.getPosition();
            
            int startPos = new SentenceMotion(false, true).destination(editorAdaptor, cursor).getModelOffset();
            int endPos = new SentenceMotion(true, true).destination(editorAdaptor, count, cursor).getModelOffset();

            if (! outer) {
                final TextContent modelContent = editorAdaptor.getModelContent();
                final int cursorIndex = cursor.getModelOffset();
                //strip trailing spaces that exist between sentences
                String text = modelContent.getText(cursorIndex, endPos - cursorIndex);
                endPos = cursorIndex + text.replaceFirst("\\s+$", "").length();
            }
            
            return new StartEndTextRange(cursor.setModelOffset(startPos), cursor.setModelOffset(endPos));
        }

        @Override
        public ContentType getContentType(final Configuration configuration) {
            return ContentType.TEXT;
        }
    }
}
