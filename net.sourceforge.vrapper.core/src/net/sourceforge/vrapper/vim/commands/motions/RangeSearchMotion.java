package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.SearchCommandParser;

/**
 * Find a string the user entered as a start or end to a range.
 * (no need to bring up SearchMode or track this as the last search)
 */
public class RangeSearchMotion implements Motion {
	
	private String toFind;
	private Position start;
	private boolean reverse;

	public RangeSearchMotion(String toFind, Position start, boolean reverse) {
		this.toFind = toFind;
		this.start = start;
		this.reverse = reverse;
	}
	
    public Position destination(EditorAdaptor editorAdaptor)
    		throws CommandExecutionException {
        Search search = SearchCommandParser.createSearch(editorAdaptor, toFind,
        		reverse, false, SearchOffset.NONE);
        SearchResult result = editorAdaptor.getSearchAndReplaceService().find(search, start);
        return result == null || result.getStart() == null ? null : result.getLeftBound();
    }

    public BorderPolicy borderPolicy() {
    	return BorderPolicy.LINE_WISE;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.NEVER;
    }

    public boolean isJump() {
        return true;
    }

	public Motion withCount(int count) {
		return this;
	}

	public int getCount() {
		return 0;
	}
}
