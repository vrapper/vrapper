package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;

public interface SearchAndReplaceService {

    /**
     * Searches for the a keyword.
     *
     * @param search
     *            the parameters of the search.
     * @param offset
     *            where to start searching.
     * @return the index of the searched string.
     */
	SearchResult find(Search search, Position start);

	/**
	 * Highlights all matches of the given search.
	 */

	void highlight(Search search);
	/**
	 * Removes all decoration from the search results.
	 */
	void removeHighlighting();

}
