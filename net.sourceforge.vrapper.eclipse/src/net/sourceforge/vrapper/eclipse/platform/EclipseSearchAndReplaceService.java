package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.Options;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

public class EclipseSearchAndReplaceService implements SearchAndReplaceService {

    private static final String ANNOTATION_TYPE = "net.sourceforge.vrapper.eclipse.searchhighlight";
    private final FindReplaceDocumentAdapter adapter;
    private final HighlightingService highlightingService;
    final Configuration sharedConfiguration;
    private Search lastHighlightedSearch;
    private List<Object> annotations;
    private Object incSearchAnnotation;

    public EclipseSearchAndReplaceService(ITextViewer textViewer, final Configuration sharedConfiguration,
            HighlightingService highlightingService) {
        this.adapter = new FindReplaceDocumentAdapter(textViewer.getDocument());
        this.highlightingService = highlightingService;
        this.sharedConfiguration = sharedConfiguration;
        this.annotations = Collections.emptyList();
    }

    public SearchResult find(Search search, Position start) {
        try {
            IRegion result = find(search, start.getModelOffset());
            Position resultPosition = result != null ? start.setModelOffset(result.getOffset()) : null;
            Position endPosition = result != null ? start.setModelOffset(result.getOffset()+result.getLength()) : null;
            return new SearchResult(resultPosition, endPosition);
        } catch (BadLocationException e) {
            return new SearchResult(null, null);
        }
    }
    
    public int replace(LineInformation line, String toFind, String replace, String flags) {
    	int start = line.getBeginOffset();
    	int end = line.getEndOffset();
    	final boolean replaceAll = flags.contains("g");
        boolean caseSensitive = isCaseSensitive(toFind, flags);

    	//each time we perform a replace,
    	//how many characters will be added/removed?
    	int lengthDiff = replace.length() - toFind.length();
    	int match;
    	int numReplaces = 0;
    	toFind = convertRegexSearch(toFind, replace);
    	try {
    		while(start < end) {
    			IRegion result = adapter.find(start, toFind, true, caseSensitive, false, true);
    			if(result != null && result.getOffset() < end) {
    				match = result.getOffset();
    				numReplaces++;
    				adapter.replace(replace, true);
    				if(replaceAll) {
    					//don't match on the replacement string
    					//when we come around again
    					// (s/foo/barfoo/g)
    					start = match + replace.length();
    					//the offset for the end of this line has changed
    					end += lengthDiff;
    				}
    				else {
    					//if not global, we've performed our one replace
    					break;
    				}
    			}
    			else {
    				//no match found
    				break;
    			}
    		}
        } catch (BadLocationException e) {
        	//should we log something?
        }
    	
    	return numReplaces;
    }

	private boolean isCaseSensitive(String toFind, String flags) {
		boolean caseSensitive = !sharedConfiguration.get(Options.IGNORE_CASE)
            || (sharedConfiguration.get(Options.SMART_CASE)
                && StringUtils.containsUppercase(toFind));
        if (flags.contains("i"))
            caseSensitive = false;
        if (flags.contains("I"))
            caseSensitive = true;
		return caseSensitive;
	}
    
    public boolean substitute(int start, String toFind, String flags, String toReplace) {
    	boolean success = false;
    	try {
    		toFind = convertRegexSearch(toFind, toReplace);
			IRegion result = adapter.find(start, toFind, true, isCaseSensitive(toFind, flags), false, true);
			if(result != null) {
				adapter.replace(toReplace, true);
				success = true;
			}
		} catch (BadLocationException e) {
		}
    	return success;
    }

    private IRegion find(Search search, int begin) throws BadLocationException {
    	if(search.isRegExSearch()) {
    		search = convertRegexSearch(search);
    	}
        IRegion result = adapter.find(
                    begin, search.getKeyword(),
                    !search.isBackward(), search.isCaseSensitive(),
                    search.isWholeWord(), search.isRegExSearch());
        return result;
    }
    
    /**
     * We're using Eclipse's (Java's) Regex engine for search/replace. However,
     * a lot of people are used to Vim's syntax for regex. So, check for Vim
     * regex syntax and map to Eclipse's (Java's) syntax where possible.
     * \< and \> =  \b   = word boundary
     * \( and \) = (   ) = create group
     *  ( and )  = \( \) = parentheses characters
     */
    private Search convertRegexSearch(Search search) {
    	String keyword = search.getKeyword();
    	keyword = convertRegexSearch(keyword, null);
    	return new Search(keyword, search.isBackward(), search.isWholeWord(),
            search.isCaseSensitive(), search.getSearchOffset(), search.isRegExSearch());
    }
    private String convertRegexSearch(String keyword, String replace) {
    	//if Vim-style groupings defined       [ s/\(foo\)/\1/ ],
    	//     convert to Eclipse-style regex  [ s/(foo)/\1/   ]
    	//     (Eclipse actually supports \1 and $1 syntax so I can leave that alone)
    	//else, no groupings being defined   [ s/(foo)/(bar)/ ]
    	//     search on literal parentheses [ s/\(foo\)/(bar)/ ]
    	//in Vim, '( )' are parentheses and '\( \)' are groups
    	//in Eclipse, '( )' are groups and '\( \)' are parentheses
    	if(replace != null) {
    		if(replace.contains("\\1")) {
    			//if using Vim-style replacements, expect Vim-style grouping definition
    			//(and convert to Eclipse-style grouping)
    			keyword = keyword.replaceAll("\\\\\\(", "(").replaceAll("\\\\\\)", ")");
    		}
    		else if( (!replace.contains("$1")) && keyword.contains("(") && (!keyword.contains("\\(")) ) {
    			//if Eclipse-style replacements aren't defined and '(' isn't escaped,
    			//escape '(' so Eclipse treats it as a '(' character
    			//(Vim would've considered this a grouping definition but in Eclipse it's escaping the character)
    			keyword = keyword.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
    		}
    	}
    	//in Vim, '\<' and '\>' are word boundaries
    	//in Eclipse, '\b' is word boundaries
    	return keyword.replaceAll("\\<|\\>", "\\b");
    }

    public void removeHighlighting() {
        lastHighlightedSearch = null;
        for (Object a : annotations) {
            highlightingService.removeHighlighting(a);
        }
        annotations = Collections.emptyList();
    }


    public void highlight(Search search) {

        if (lastHighlightedSearch != null && lastHighlightedSearch.getKeyword().equals(search.getKeyword())
                && lastHighlightedSearch.isCaseSensitive() == search.isCaseSensitive()
                && lastHighlightedSearch.isWholeWord() == search.isWholeWord()) {
            return;
        }
        removeHighlighting();
        if (search.isBackward()) {
            search = search.reverse();
        }
        IRegion result = new Region(0, 0);
        annotations = new ArrayList<Object>();
        lastHighlightedSearch = search;
        try {
            while ((result = find(search, result.getOffset()+result.getLength())) != null) {
                Object a = highlightingService.highlightRegion(ANNOTATION_TYPE,
                        "Vrapper Search", result.getOffset(), result.getLength());
                annotations.add(a);
            }
        } catch (BadLocationException e) {
            VrapperLog.error("while highlighting search", e);
        }
    }
    public void incSearchhighlight(Position start, int length) {
        removeIncSearchHighlighting();
        incSearchAnnotation = highlightingService.highlightRegion(ANNOTATION_TYPE,
                "Incremental Search", start.getModelOffset(), length);
    }

    public void removeIncSearchHighlighting() {
        if (incSearchAnnotation != null) {
            highlightingService.removeHighlighting(incSearchAnnotation);
        }
    }

}
