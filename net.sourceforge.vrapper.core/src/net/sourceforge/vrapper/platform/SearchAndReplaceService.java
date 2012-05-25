package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.LineInformation;
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
	 * Perform a search and replace.
	 * @param line line in file to search for toFind
	 * @param toFind String to find in the line
	 * @param replace String to replace toFind with
	 * @param flags Regex flags like 'g' for global and 'i' for insensitive case
	 * @return true if match found (and replaced), false if no match
	 */
    boolean replace(LineInformation line, String toFind, String replace, String flags);

	/**
	 * Highlights all matches of the given search.
	 */
	void highlight(Search search);

	/**
	 * Removes all decoration from the search results.
	 */
	void removeHighlighting();

	/**
	 * Highlights the given region. Used for incremental search.
	 */
	void incSearchhighlight(Position start, int length);

	/**
	 * Removes the highlighting set with {@link #incSearchhighlight(Position, int)}.
	 */
	void removeIncSearchHighlighting();

}
