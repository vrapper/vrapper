package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.commandline.SearchCommandParser;

/**
 * ':/' behaves very similarly to '/' without entering search mode. When you
 * first run it, the cursor goes to the first non-whitespace character on the
 * line of the first match.  If you hit 'n' or 'N' the cursor will move to the
 * beginning of the next match just like regular search mode.  Also, search
 * highlighting still highlights the matches like normal search mode.
 * So this class needs to perform a search as if it were in search mode
 * but move the cursor to the beginning of the line for that first match.
 */
public class ExSearchCommand extends CountAwareCommand {
    
    private final String first;
    private final String command;
    
    public ExSearchCommand(String first, String command) {
        this.first = first;
        this.command = command;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        //get cursor position first so we'll know if it changed
        Position cursor = editorAdaptor.getPosition();

        //perform a search as if SearchMode initiated it
        //(this ensures the correct flag parsing, search highlighting, and history)
        SearchCommandParser parser = new SearchCommandParser(editorAdaptor, new MotionCommand(SearchResultMotion.REPEAT));
        parser.parseAndExecute(first, command).execute(editorAdaptor);
        
        //check to see if cursor moved (a match was found)
        if(cursor.getModelOffset() != editorAdaptor.getPosition().getModelOffset()) {
            TextContent model = editorAdaptor.getModelContent();

            //find first non-whitespace character on the line with the match
            cursor = editorAdaptor.getPosition();
            LineInformation line = model.getLineInformationOfOffset(cursor.getModelOffset());
            int newPos = VimUtils.getFirstNonWhiteSpaceOffset(model, line);

            //move cursor to the beginning of the line of this first match
            editorAdaptor.setPosition(editorAdaptor.getPosition().setModelOffset(newPos), StickyColumnPolicy.ON_CHANGE);
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
