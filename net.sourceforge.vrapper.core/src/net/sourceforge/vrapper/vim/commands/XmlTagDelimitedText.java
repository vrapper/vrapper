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
    	String tag = editorAdaptor.getModelContent().getText(endTag);
    	String tagName = tag.substring(2, tag.length()-1);
    	
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
     * Don't just find the next closing tag,
     * find the closing tag that we're inside.
     */
    private TextRange findInsideClosingTag(EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Position cursorPos = editorAdaptor.getCursorService().getPosition();
    	Position start = cursorPos;
    	TextRange closeTag;
    	String tagName;
    	
    	do {
    		closeTag = findNextClosingTag(start, editorAdaptor);
    		String tag = editorAdaptor.getModelContent().getText(closeTag);
    		tagName = tag.substring(2, tag.length()-1);
    		
    		//prepare to loop again just in case
    		start = closeTag.getRightBound();
    		
    		//if we aren't inside this tag, look for the next closing tag 
    	} while( isOpenTagAfterCursor(cursorPos, closeTag.getLeftBound(), tagName, editorAdaptor) );
    	
    	return closeTag;
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
     * We found a closing tag, but is its opening tag after the cursor?
     */
    private boolean isOpenTagAfterCursor(Position start, Position tagStart, String tagName, EditorAdaptor editorAdaptor) {
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result;
    	
    	Search findTagStart = new Search("<"+tagName, false, false, true, SearchOffset.NONE, false);
    	result = searchAndReplace.find(findTagStart, start);
    	
    	//is this open tag before the closing tag we'd found?
    	return result.isFound() && result.getRightBound().getModelOffset() < tagStart.getModelOffset();
    }
    
    /**
     * Find the first open tag for tagName before the cursor.
     */
    private TextRange findInsideStartTag(String tagName, EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Position cursorPos = editorAdaptor.getCursorService().getPosition();
    	SearchAndReplaceService searchAndReplace = editorAdaptor.getSearchAndReplaceService();
    	SearchResult result;
    	
    	Search findTagStart = new Search("<"+tagName, true, false, true, SearchOffset.NONE, false);
    	result = searchAndReplace.find(findTagStart, cursorPos);
    	if( ! result.isFound()) {
            throw new CommandExecutionException("Could not find open tag to match </"+tagName+">");
    	}
    	Position tagStart = result.getLeftBound();
    	
    	//There might be an arbitrary number of attributes set on this tag.
    	//Don't assume it's "<"+tagName+">"
    	Search findTagEnd = new Search(">", false, false, true, SearchOffset.NONE, false);
    	result = searchAndReplace.find(findTagEnd, result.getRightBound());
    	if( ! result.isFound()) {
            throw new CommandExecutionException("Could not find ending to <"+tagName);
    	}
    	Position tagEnd = result.getRightBound();
    	
    	return new StartEndTextRange(tagStart, tagEnd);
    }
}
