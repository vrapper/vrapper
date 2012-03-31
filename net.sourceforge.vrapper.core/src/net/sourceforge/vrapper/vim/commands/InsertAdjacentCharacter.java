package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * While in InsertMode, you can hit Ctrl-E or Ctrl-Y.
 * Ctrl-E will insert the character in the line below the cursor,
 * Ctrl-Y will insert the character in the line above the cursor.
 * You remain in InsertMode the entire time.
 */
public class InsertAdjacentCharacter extends CountIgnoringNonRepeatableCommand {
	
	public static final InsertAdjacentCharacter LINE_ABOVE = new InsertAdjacentCharacter(true);
	public static final InsertAdjacentCharacter LINE_BELOW = new InsertAdjacentCharacter(false);
	
	private boolean above;
	
	private InsertAdjacentCharacter(boolean above) {
		this.above = above;
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
        CursorService cursorService = editorAdaptor.getCursorService();
        TextContent content = editorAdaptor.getViewContent();
        
        int cursorOffset = cursorService.getPosition().getViewOffset();
        int currentLineNumber = content.getLineInformationOfOffset(cursorOffset).getNumber();
        LineInformation adjacentLine;
        if(above && currentLineNumber > 0) {
        	adjacentLine = content.getLineInformation( currentLineNumber - 1 );
        }
        else if( (!above) && currentLineNumber < content.getNumberOfLines()) {
        	adjacentLine = content.getLineInformation( currentLineNumber + 1 );
        }
        else {
        	//no adjacent line, abort
        	return;
        }
        
        Position adjacentPos = cursorService.stickyColumnAtViewLine(adjacentLine.getNumber());
        int adjacentOffset = adjacentPos.getViewOffset();
        //have we reached EOF? (this will throw an Exception if we try)
        if(adjacentOffset < content.getTextLength()-1) {
        	String adjacentChar = content.getText(adjacentOffset, 1);
        	if(VimUtils.isNewLine(adjacentChar)) {
        		//the adjacent line ended before the current cursor position
        		return;
        	}

        	//insert this adjacent character at the cursor position
        	content.smartInsert(adjacentChar);
        	//update stickyColumn in case the user does this operation back-to-back
        	cursorService.setPosition(cursorService.newPositionForViewOffset(cursorOffset+1), true);
        }
	}

}
