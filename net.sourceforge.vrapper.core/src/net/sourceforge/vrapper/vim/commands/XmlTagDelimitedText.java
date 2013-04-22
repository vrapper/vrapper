package net.sourceforge.vrapper.vim.commands;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * The cursor is inside a pair of XML tags.  Find the open tag *before* the cursor
 * that matches the closing tag *after* the cursor.  Note that We don't know which
 * open tag name we're looking for until we find the unbalanced closing tag after
 * the cursor.  This is to handle malformed XML documents with lingering open tags.
 * This aligns with how Vim handles things.
 */
public class XmlTagDelimitedText implements DelimitedText {
    
    //regex usually stops at newlines but open tags might have
    //multiple lines of attributes.  So, include newlines in search.
    
    private static final String XML_TAG_REGEX = "<([^<]|\n)*?>";
    
    private static final Pattern tagPattern = Pattern.compile(XML_TAG_REGEX);
    
	private static final String XML_TAG_NAME_REGEX = "([^ \n\t]*)";
    private static final String XML_PARAMETERS_REGEX = "(\n|.)*";
    private static final String XML_NAME_REGEX = "</?" + XML_TAG_NAME_REGEX + XML_PARAMETERS_REGEX + ">";
    
    private static final Pattern tagNamePattern = Pattern.compile(XML_NAME_REGEX);
    
    private TextRange endTag;
    private TextRange openTag;
	
    public TextRange leftDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	calculateOpenAndCloseTag(editorAdaptor, count);
        return openTag;
    }

    public TextRange rightDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        calculateOpenAndCloseTag(editorAdaptor, count);
        return endTag;
    }
    
    private void calculateOpenAndCloseTag(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if(count == 0) {
    		count = 1;
    	}
    	
    	Position beginningPosition = getStartingPosition(editorAdaptor);
    	
    	Position startOpenSearch = beginningPosition.addModelOffset(1);
    	Position startCloseSearch = beginningPosition.addModelOffset(-1);

    	for(int i=0; i < count; i++) {
    		//Vim first looks left for an opening tag to determine what close tag to look for
    		openTag = getUnbalancedOpenTag(startOpenSearch, editorAdaptor);
    		String tagName = getTagName(openTag, editorAdaptor);
    		
    		//find the first unbalanced closing tag after start
    		//that matches the name of the opening tag we found
    		endTag = getUnbalancedClosingTag(startCloseSearch, tagName, editorAdaptor);
    		
    		//prepare for next iteration (if any)
    		//to find the parent open and closing tags to the ones we just found
    		startOpenSearch = openTag.getLeftBound();
    		startCloseSearch = endTag.getRightBound();
    	}
    }

    /**
     * Account for the possibility that we're inside an opening or closing tag
     */
    private Position getStartingPosition(EditorAdaptor editorAdaptor) {
        Position beginningPosition = editorAdaptor.getCursorService().getPosition();
    	
    	if (insideOpeningTag(beginningPosition, editorAdaptor)) {
    	    //move to the end of the opening tag we're inside
    	    while (characterAt(beginningPosition,editorAdaptor) != '>') {
    	        beginningPosition = beginningPosition.addModelOffset(1);
    	    }
    	}
    	
    	if (insideClosingTag(beginningPosition, editorAdaptor)) {
    	    //move to the beginning of the closing tag we're inside
    	    while (characterAt(beginningPosition,editorAdaptor) != '<') {
    	        beginningPosition = beginningPosition.addModelOffset(-1);
    	    }
    	}
        return beginningPosition;
    }
    
    private boolean insideOpeningTag(Position position, EditorAdaptor editorAdaptor) {
        return insideTag(position, editorAdaptor, true);
    }
    
    private boolean insideClosingTag(Position position, EditorAdaptor editorAdaptor) {
        return insideTag(position, editorAdaptor, false);
    }
    
    private boolean insideTag(Position position, EditorAdaptor editorAdaptor, boolean openingTag) {
        return (toRightOfTagOpener(position, editorAdaptor, openingTag) && toLeftOfTagCloser(position, editorAdaptor));
    }

    private boolean toLeftOfTagCloser(Position position, EditorAdaptor editorAdaptor) {
        while (characterAt(position, editorAdaptor) != '>') {
           position = position.addModelOffset(1);
           if (position.getModelOffset() >= editorAdaptor.getModelContent().getTextLength()) {
               return false;
           }
           
           if (characterAt(position, editorAdaptor) == '<') {
               return false;
           }
        }
        return true;
    }

    private boolean toRightOfTagOpener(Position position, EditorAdaptor editorAdaptor, boolean openingTag) {
        while (characterAt(position, editorAdaptor) != '<') {
           position = position.addModelOffset(-1);
           if (position.getModelOffset() < 0) {
               return false;
           }
           
           if (characterAt(position, editorAdaptor) == '>') {
               return false;
           }
        }
        //check if the char after '<' is '/'
        position = position.addModelOffset(1); 
        if (openingTag) {
            return characterAt(position, editorAdaptor) != '/';
        } else {
            return characterAt(position, editorAdaptor) == '/';
        }
    }

    private char characterAt(Position position, EditorAdaptor editorAdaptor) {
        return editorAdaptor.getModelContent().getText(position.getModelOffset(), 1).charAt(0);
    }

    /**
     * Search backwards for XML tags.  Push every close tag, pop every open tag.
     * If we get the open tag we're looking for without a matching close tag, we're inside that tag.
     */
    public TextRange getUnbalancedOpenTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Stack<String> closeTags = new Stack<String>();
    	TextRange tag;
    	String contents;
    	String tagName;
    	
    	while (true) { //we'll either hit a 'return' or throw an exception
    		tag = findPreviousTag(start, editorAdaptor);
    		contents = editorAdaptor.getModelContent().getText(tag);
    		start = tag.getLeftBound(); //prepare for next iteration
    		
    		if(isCloseTag(contents)) {
    			tagName = getTagName(tag, editorAdaptor);
    			closeTags.push(tagName);
    		}
    		else { //open tag
    			tagName = getTagName(tag, editorAdaptor);
    			if(closeTags.empty()) {
    				//we hit the desired open tag before finding any close tags
    				//the cursor must be inside this tag
    				return tag;
    			}
    			else if (closeTags.peek().equals(tagName)) {
    				//found the matching open tag for this close tag
    				//ignore it and keep moving
    				closeTags.pop();
    			}
    			else {
    				//we found an open tag without a corresponding close tag
    				//but it wasn't the open tag we wanted
    				//ignore it and keep moving (this is how vim handles it)
    			}
    		}
    	}
    }

    private boolean isCloseTag(String contents) {
        return contents.startsWith("</");
    }
    
    /**
     * Search forwards for XML tags.  Push every open tag, pop every close tag.
     * If we get a close tag without an open tag, we're inside that tag.
     */
    public TextRange getUnbalancedClosingTag(Position start, String toFindTagName, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Stack<String> openTags = new Stack<String>();
    	TextRange tag;
    	String contents;
    	String tagName;
    	
    	while (true) { //we'll either hit a 'return' or throw an exception
    		tag = findNextTag(start, editorAdaptor);
    		contents = editorAdaptor.getModelContent().getText(tag);
    		start = tag.getRightBound(); //prepare for next iteration

    		if(isCloseTag(contents)) {
    			tagName = getTagName(tag, editorAdaptor);
    			if(tagName.equals(toFindTagName) && openTags.empty()) {
    				//we hit a close tag before finding any open tags
    				//the cursor must be inside this tag
    				return tag;
    			}
    			else if(tagName.equals(toFindTagName)) {
    				//found the matching close tag for this open tag
    				//ignore it and keep moving
    				openTags.pop();
    			}
    		}
    		else { //open tag, see if we'll find it's matching close tag
    			tagName = getTagName(tag, editorAdaptor);
    			if (tagName.equals(toFindTagName)) {
    			    openTags.push(tagName);
    			}
    		}
    	}
    }
    
    
    /**
     * Search for the next XML tag after start.  Can either be an open tag or
     * close tag.  We'll let the calling method figure out what to do with it.
     */
    private TextRange findNextTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
        TextContent content = editorAdaptor.getModelContent();
        int startPos = start.getModelOffset();
        String textAfterStartPos = getText(editorAdaptor, startPos, content.getTextLength());
        return rangeForFirstXmlTagWithOffset(editorAdaptor, textAfterStartPos, startPos);
    }

    private TextRange rangeForFirstXmlTagWithOffset(EditorAdaptor editorAdaptor, String text, int startPos) throws CommandExecutionException {
        Matcher matcher = tagPattern.matcher(text);
        if (matcher.find()) {
            int matchStart = matcher.start() + startPos;
            int matchEnd = matcher.end() + startPos;
            return getRange(editorAdaptor, matchStart, matchEnd);
        } else {
            throw new CommandExecutionException("The cursor is not within an XML tag");
        }
    }
    
    /**
     * Search for the previous XML tag before start.  Can either be an open tag or
     * close tag.  We'll let the calling method figure out what to do with it.
     */
    private TextRange findPreviousTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
        int startPos = start.getModelOffset();
        String textBeforeStartPos = getText(editorAdaptor, 0, startPos);
        return rangeForLastXmlTag(editorAdaptor, textBeforeStartPos);
    }

    private TextRange rangeForLastXmlTag(EditorAdaptor editorAdaptor, String text) throws CommandExecutionException {
        Matcher matcher = tagPattern.matcher(text);
        TextRange range = null;
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            range = getRange(editorAdaptor, matchStart, matchEnd);
        }
        
        if (range != null) {
            return range;
        } else {
            throw new CommandExecutionException("The cursor is not within an XML tag");
        }
    }
    
    private String getText(EditorAdaptor editorAdaptor, int from, int to) {
        return editorAdaptor.getModelContent().getText(from, to - from);
    }

    /**
     * @param tagRange The range the tag lies within
     * @return The contents of the tag
     */
    private String getTagName(TextRange tagRange, EditorAdaptor editorAdaptor) {
    	String contents = editorAdaptor.getModelContent().getText(tagRange);
    	Matcher matcher = tagNamePattern.matcher(contents);
    	if (matcher.find()) {
    	    return matcher.group(1);
    	}
    	return "";
    }
    
    private TextRange getRange(EditorAdaptor editorAdaptor, int start, int end) {
        Position matchBegin = editorAdaptor.getPosition().setModelOffset(start);
        Position matchEnd   = editorAdaptor.getPosition().setModelOffset(end);
        return new StartEndTextRange(matchBegin, matchEnd);
    }
}
