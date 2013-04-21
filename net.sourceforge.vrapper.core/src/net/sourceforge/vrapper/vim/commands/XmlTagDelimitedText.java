package net.sourceforge.vrapper.vim.commands;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
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
    private TextRange endTag;
	
	/**
	 * Find the open tag this cursor is inside.  To do that,
	 * we first have to find the closing tag we're inside.
	 */
    public TextRange leftDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	if(count == 0) {
    		count = 1;
    	}
    	
    	String tagName;
    	TextRange openTag = null;
    	Position startOpenSearch = editorAdaptor.getCursorService().getPosition();
    	Position startCloseSearch = editorAdaptor.getCursorService().getPosition();
    	for(int i=0; i < count; i++) {
    		//find the first unbalanced closing tag after start
    		endTag = getUnbalancedClosingTag(startCloseSearch, editorAdaptor);
    		tagName = getClosingTagName(endTag, editorAdaptor);
    		//find the first unbalanced open tag before start that
    		//matches the name of the closing tag we found
    		openTag = getUnbalancedOpenTag(startOpenSearch, tagName, editorAdaptor);
    		
    		//prepare for next iteration (if any)
    		//to find the parent open and closing tags to the ones we just found
    		startOpenSearch = openTag.getLeftBound();
    		startCloseSearch = endTag.getRightBound();
    	}
    	
        return openTag;
    }

    /**
     * Find the closing tag this cursor is inside.
     */
    public TextRange rightDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	if(count == 0) {
    		count = 1;
    	}
    	
    	if(endTag != null && count == 1) {
    		//if someone already ran leftDelimiter(), just re-use that tag
    		return endTag;
    	}
    	else {
    		TextRange closeTag = null;
    		Position start = editorAdaptor.getCursorService().getPosition();
    		for(int i=0; i < count; i++) {
    			closeTag = getUnbalancedClosingTag(start, editorAdaptor);
    			//prepare for next iteration (if any)
    			start = closeTag.getRightBound();
    		}
    		return closeTag;
    	}
    }
    
    /**
     * Search forwards for XML tags.  Push every open tag, pop every close tag.
     * If we get a close tag without an open tag, we're inside that tag.
     */
    public TextRange getUnbalancedClosingTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Stack<String> openTags = new Stack<String>();
    	TextRange tag;
    	String contents;
    	String tagName;
    	
    	while (true) { //we'll either hit a 'return' or throw an exception
    		tag = findNextTag(start, editorAdaptor);
    		contents = editorAdaptor.getModelContent().getText(tag);
    		start = tag.getRightBound(); //prepare for next iteration

    		if(contents.startsWith("</")) { //close tag
    			tagName = getClosingTagName(tag, editorAdaptor);
    			if(openTags.empty()) {
    				//we hit a close tag before finding any open tags
    				//the cursor must be inside this tag
    				return tag;
    			}
    			else if(openTags.peek().equals(tagName)) {
    				//found the matching close tag for this open tag
    				//ignore it and keep moving
    				openTags.pop();
    			}
    			else {
    				//there must've been a mis-matched open tag
    				//does this closing tag match with anything we've seen?
    				while(!openTags.empty()) {
    					if(openTags.peek().equals(tagName)) {
    						//we found the match to this close tag
    						//just skip that unexpected open tag
    						break;
    					}
    					//see if the previous tag matches
    					//this closing tag's name
    					openTags.pop();
    				}
    				//did we exhaust the list?
    				//this must be the closing tag we're inside
    				if(openTags.empty()) {
    					return tag;
    				}
    			}
    		}
    		else { //open tag, see if we'll find it's matching close tag
    			tagName = getOpenTagName(tag, editorAdaptor);
    			openTags.push(tagName);
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
        
        String text = content.getText(startPos, content.getTextLength() - startPos);
        
        Pattern tag = Pattern.compile(XML_TAG_REGEX);
        Matcher matcher = tag.matcher(text);
        
        if (matcher.find()) {
            int matchStart = matcher.start() + startPos;
            int matchEnd = matcher.end() + startPos;
            return getRange(editorAdaptor, matchStart, matchEnd);
        } else {
    		//we couldn't find a tag
            throw new CommandExecutionException("The cursor is not within an XML tag");
        }
    }

    private TextRange getRange(EditorAdaptor editorAdaptor, int start, int end) {
        Position matchBegin = editorAdaptor.getPosition().setModelOffset(start);
        Position matchEnd   = editorAdaptor.getPosition().setModelOffset(end);
        return new StartEndTextRange(matchBegin, matchEnd);
    }
    
    /**
     * Search for the previous XML tag before start.  Can either be an open tag or
     * close tag.  We'll let the calling method figure out what to do with it.
     */
    private TextRange findPreviousTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
        TextContent content = editorAdaptor.getModelContent();
        int startPos = start.getModelOffset();
        
        String text = content.getText(0, startPos);
        
        Pattern tag = Pattern.compile(XML_TAG_REGEX);
        Matcher matcher = tag.matcher(text);
        
        
        // grab the last match
        TextRange range = null;
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            range = getRange(editorAdaptor, matchStart, matchEnd);
        } 
        
        if (range != null) {
            return range;
        } else {
            //we couldn't find a tag
            throw new CommandExecutionException("The cursor is not within an XML tag");
        }
    }
    
    /**
     * Search backwards for XML tags.  Push every close tag, pop every open tag.
     * If we get the open tag we're looking for without a matching close tag, we're inside that tag.
     */
    public TextRange getUnbalancedOpenTag(Position start, String toFindTagName, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Stack<String> closeTags = new Stack<String>();
    	TextRange tag;
    	String contents;
    	String tagName;
    	
    	while (true) { //we'll either hit a 'return' or throw an exception
    		tag = findPreviousTag(start, editorAdaptor);
    		contents = editorAdaptor.getModelContent().getText(tag);
    		start = tag.getLeftBound(); //prepare for next iteration
    		
    		if(contents.startsWith("</")) { //close tag, see if we'll find it's matching open tag
    			tagName = getClosingTagName(tag, editorAdaptor);
    			closeTags.push(tagName);
    		}
    		else { //open tag
    			tagName = getOpenTagName(tag, editorAdaptor);
    			if(closeTags.empty() && tagName.equals(toFindTagName) ) {
    				//we hit the desired open tag before finding any close tags
    				//the cursor must be inside this tag
    				return tag;
    			}
    			else if(!closeTags.empty() && closeTags.peek().equals(tagName)) {
    				//found the matching open tag for this close tag
    				//ignore it and keep moving
    				closeTags.pop();
    			}
    			else if(tagName.equals(toFindTagName)) {
    				//we don't have the corresponding close tag for this open tag
    				//and it matches the name we're looking for
    				//the cursor must be inside this tag
    				return tag;
    			}
    			else {
    				//we found an open tag without a corresponding close tag
    				//but it wasn't the open tag we wanted
    				//ignore it and keep moving (this is how vim handles it)
    			}
    		}
    	}
    }
    
    /**
     * Just a few convenience methods
     */
    private String getOpenTagName(TextRange tagRange, EditorAdaptor editorAdaptor) {
    	String contents = editorAdaptor.getModelContent().getText(tagRange);
    	//name goes from '<' to first space or first '>'
    	return contents.indexOf(' ') > -1 ? contents.substring(1, contents.indexOf(' ')) : contents.substring(1, contents.indexOf('>'));
    }
    
    private String getClosingTagName(TextRange tagRange, EditorAdaptor editorAdaptor) {
    	String tag = editorAdaptor.getModelContent().getText(tagRange);
    	//chop off leading '</' and trailing '>'
    	return tag.substring(2, tag.length()-1);
    }
}
