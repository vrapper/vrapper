package net.sourceforge.vrapper.vim.commands;

import java.util.Stack;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * The cursor is inside a pair of XML tags.  Find the open tag before the cursor
 * that matches the closing tag after the cursor.  We don't know which tag name we're looking
 * for until we find the unbalanced closing tag after the cursor.
 */
public class XmlTagDelimitedText implements DelimitedText {
	
	private TextRange endTag;
	
    public TextRange leftDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	Position cursorPos = editorAdaptor.getCursorService().getPosition();
    	endTag = getUnbalancedClosingTag(cursorPos, editorAdaptor);
    	String tagName = getClosingTagName(endTag, editorAdaptor);
    	
        return getUnbalancedOpenTag(cursorPos, tagName, editorAdaptor);
    }

    public TextRange rightDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	if(endTag != null) {
    		//if someone already ran leftDelimiter(), just re-use that tag
    		return endTag;
    	}
    	else {
    		Position cursorPos = editorAdaptor.getCursorService().getPosition();
    		return getUnbalancedClosingTag(cursorPos, editorAdaptor);
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
    	
    	while(true) { //we'll either hit a 'return' or throw an exception
    		tag = findNextTag(start, editorAdaptor);
    		contents = editorAdaptor.getModelContent().getText(tag);
    		start = tag.getRightBound(); //prepare for next iteration
    		
    		if(contents.startsWith("</")) { //close tag
    			tagName = getClosingTagName(tag, editorAdaptor);
    			if(openTags.empty() ) {
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
    						break;
    					}
    					openTags.pop();
    				}
    				//did we exhaust the list?
    				//this must be the closing tag we're inside
    				if(openTags.empty()) {
    					return tag;
    				}
    			}
    		}
    		else { //open tag
    			tagName = getOpenTagName(tag, editorAdaptor);
    			openTags.push(tagName);
    		}
    	}
    }
    
    private TextRange findNextTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Search findTag = new Search("<.*?>", false, false, true, SearchOffset.NONE, true);
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result = searchAndReplace.find(findTag, start);
    	if( ! result.isFound()) {
    		//we couldn't find a tag
            throw new CommandExecutionException("The cursor is not within an XML tag");
    	}
    	
    	return new StartEndTextRange(result.getLeftBound(), result.getRightBound());
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
    	
    	while(true) { //we'll either hit a 'return' or throw an exception
    		tag = findPreviousTag(start, editorAdaptor);
    		contents = editorAdaptor.getModelContent().getText(tag);
    		start = tag.getLeftBound(); //prepare for next iteration
    		
    		if(contents.startsWith("</")) { //close tag
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
    
    private TextRange findPreviousTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Search findTag = new Search("<.*?>", true, false, true, SearchOffset.NONE, true);
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result = searchAndReplace.find(findTag, start);
    	if( ! result.isFound()) {
    		//we couldn't find a tag
            throw new CommandExecutionException("The cursor is not within an XML tag");
    	}
    	
    	return new StartEndTextRange(result.getLeftBound(), result.getRightBound());
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
