package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class XmlTagDelimitedText implements DelimitedText {
	
	private TextRange endTag;

    public TextRange leftDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	//find nearest closing tag to know which opening tag we need
    	endTag = findInsideClosingTag(editorAdaptor);
    	String tagName = getClosingTagName(endTag, editorAdaptor);
    	
    	//go find the open tag that matches this endTag we're inside
        return findInsideStartTag(tagName, editorAdaptor);
    }

    public TextRange rightDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    	if(endTag != null) {
    		//if someone already ran leftDelimiter(), just re-use that tag
    		return endTag;
    	}
    	else {
    		return findInsideClosingTag(editorAdaptor);
    	}
    }
    
    /**
     * Don't just find the next open tag, find the open tag that we're inside.
     * (find the first unbalanced open tag before cursor)
     */
    private TextRange findInsideStartTag(String tagName, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Position cursorPos = editorAdaptor.getCursorService().getPosition();
    	Position before = cursorPos;
    	Position after = cursorPos;
    	TextRange openTag;
    	
    	do {
    		before = after;
    		openTag = findNextOpenTag(before, tagName, editorAdaptor);
    		
    		//prepare to loop again just in case
    		after = openTag.getLeftBound();
    		
    		//if we aren't inside this tag, look for the next open tag 
    	} while( isClosingTagBeforeCursor(after, before, tagName, editorAdaptor) );
    	
    	
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	//There might be an arbitrary number of attributes set on this tag.
    	//Don't assume it's "<"+tagName+">"
    	Search findTagEnd = new Search(">", false, false, true, SearchOffset.NONE, false);
    	SearchResult result = searchAndReplace.find(findTagEnd, openTag.getRightBound());
    	if( ! result.isFound()) {
            throw new CommandExecutionException("Could not find ending to <"+tagName);
    	}
    	
    	return new StartEndTextRange(openTag.getLeftBound(), result.getRightBound());
    }
    
    /**
     * Don't just find the next closing tag, find the closing tag that we're inside.
     * (find the first unbalanced close tag after cursor)
     */
    private TextRange findInsideClosingTag(EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Position cursorPos = editorAdaptor.getCursorService().getPosition();
    	Position after = cursorPos;
    	Position before = cursorPos;
    	TextRange closeTag;
    	String tagName;
    	
    	do {
    		after = before;
    		closeTag = findNextClosingTag(after, editorAdaptor);
    		tagName = getClosingTagName(closeTag, editorAdaptor);
    		
    		//prepare to loop again just in case
    		before = closeTag.getRightBound();
    		
    		//if we aren't inside this tag, look for the next closing tag 
    	} while( isOpenTagAfterCursor(after, before, tagName, editorAdaptor) );
    	
    	return closeTag;
    }
    
    /**
     * Find the next open tag before the start Position.
     */
    private TextRange findNextOpenTag(Position start, String tagName, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	Search findTagStart = new Search("<"+tagName, true, false, true, SearchOffset.NONE, false);
    	SearchResult result = searchAndReplace.find(findTagStart, start);
    	if( ! result.isFound()) {
    		//we couldn't find an open tag
            throw new CommandExecutionException("Could not find open tag to match </"+tagName+">");
    	}
    	
    	return new StartEndTextRange(result.getLeftBound(), result.getRightBound());
    }
    
    /**
     * Find the next closing tag after the start Position.
     */
    private TextRange findNextClosingTag(Position start, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Search findEnd = new Search("</.*?>", false, false, true, SearchOffset.NONE, true);
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result = searchAndReplace.find(findEnd, start);
    	if( ! result.isFound()) {
    		//we couldn't find a closing tag
            throw new CommandExecutionException("The cursor is not within an XML tag");
    	}
    	
    	return new StartEndTextRange(result.getLeftBound(), result.getRightBound());
    }
    
    /**
     * We found an open tag, but is its closing tag before the cursor?
     */
    private boolean isClosingTagBeforeCursor(Position tagStart, Position stopLooking, String tagName, EditorAdaptor editorAdaptor) {
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result;
    	
    	Search findEnd = new Search("</"+tagName+">", false, false, true, SearchOffset.NONE, false);
    	result = searchAndReplace.find(findEnd, tagStart);
    	
    	//is this close tag after the open tag we'd found?
    	return result.isFound() && result.getRightBound().getModelOffset() < stopLooking.getModelOffset();
    }
    
    /**
     * We found a closing tag, but is its opening tag after the cursor?
     */
    private boolean isOpenTagAfterCursor(Position startLooking, Position tagStart, String tagName, EditorAdaptor editorAdaptor) {
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result;
    	
    	Search findTagStart = new Search("<"+tagName, false, false, true, SearchOffset.NONE, false);
    	result = searchAndReplace.find(findTagStart, startLooking);
    	
    	//is this open tag before the closing tag we'd found?
    	return result.isFound() && result.getRightBound().getModelOffset() < tagStart.getModelOffset();
    }
    
    /**
     * Just a convenience method
     */
    private String getClosingTagName(TextRange tagRange, EditorAdaptor editorAdaptor) {
    	String tag = editorAdaptor.getModelContent().getText(tagRange);
    	//chop off leading '</' and trailing '>'
    	return tag.substring(2, tag.length()-1);
    }
}
