package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Move the view one half-screen up or down.  Move the cursor
 * accordingly so it stays on the same visual line.
 */
public class HalfPageUpDownMotion extends GoToLineMotion {
	
    public static final HalfPageUpDownMotion PAGE_UP   = new HalfPageUpDownMotion(true);
    public static final HalfPageUpDownMotion PAGE_DOWN = new HalfPageUpDownMotion(false);
    
    private boolean pageUp;
    
    private HalfPageUpDownMotion(boolean pageUp) {
    	super();
    	this.pageUp = pageUp;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        ViewportService viewService = editorAdaptor.getViewportService();
        ViewPortInformation view = viewService.getViewPortInformation();
        //I don't know why, but it's always off-by-one
        int viewLines = view.getNumberOfLines() + 1;
        int numLinesToMove = viewLines/2; //half-screen
        if(pageUp) {
        	numLinesToMove *= -1;
        }
        
        //move view
        int topLine = view.getTopLine();
        int destTopLine = Math.max(0, topLine + numLinesToMove);
        viewService.setTopLine(destTopLine);
        
        //move cursor
        int cursor = editorAdaptor.getPosition().getViewOffset();
        int cursorLine = editorAdaptor.getViewContent().getLineInformationOfOffset(cursor).getNumber();
        int destCursorLine = viewService.viewLine2ModelLine(
        		Math.max(0, cursorLine + numLinesToMove)
        	);
        //viewLine2ModelLine returns -1 if we've exceeded the last line
        if(destCursorLine < 0) {
        	destCursorLine = editorAdaptor.getModelContent().getNumberOfLines();
        }
        // add 1 because GoToLineMotion is 1-based
        return super.destination(editorAdaptor, destCursorLine + 1);
	}

}
