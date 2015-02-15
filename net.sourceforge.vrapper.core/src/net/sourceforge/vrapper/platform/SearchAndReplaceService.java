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
	 * @return count of replacements performed
	 */
    int replace(LineInformation line, String toFind, String replace, String flags);

    /**
     * Perform a single text substitution (with regex support)
     * @param start - model index to start looking
     * @param toFind - String to search for
     * @param flags - Regex flags like 'i' for insensitive case
     * @param toReplace - String to replace 'toFind' with
     * @return true if successful, false if no substitution performed
     */
    boolean substitute(int start, String toFind, String flags, String toReplace);

    /**
     * Parse find string and flags (and use local config)
     * to determine whether the search should be case-sensitive.
     */
	boolean isCaseSensitive(String toFind, String flags);

	/**
	 * Highlights all matches of the given search and removes previous highlights (for the current
	 * editor).
	 */
	void highlight(Search search);

	/**
	 * Removes all decoration from the search results (for the current editor).
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
