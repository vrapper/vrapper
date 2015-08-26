package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.Options;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

public class EclipseSearchAndReplaceService implements SearchAndReplaceService {

    private static final String INC_ANNOTATION_TYPE = "net.sourceforge.vrapper.eclipse.incsearchhighlight";
    private static final String ANNOTATION_TYPE = "net.sourceforge.vrapper.eclipse.searchhighlight";
    private final FindReplaceDocumentAdapter adapter;
    private final HighlightingService highlightingService;
    private final Configuration configuration;
    private Search lastHighlightedSearch;
    private List<Object> annotations;
    private Object incSearchAnnotation;
    private ITextViewer textViewer;

    public EclipseSearchAndReplaceService(ITextViewer textViewer, final Configuration configuration,
            HighlightingService highlightingService) {
        this.textViewer = textViewer;
        this.adapter = new FindReplaceDocumentAdapter(textViewer.getDocument());
        this.highlightingService = highlightingService;
        this.configuration = configuration;
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
        } catch (PatternSyntaxException e) {
            throw new VrapperPlatformException("Regex uses bad format: " + e.getMessage(), e);
        }
    }
    
    public int replace(LineInformation line, String toFind, String replace, String flags) {
        int start = line.getBeginOffset();
        int end = line.getEndOffset();
        final boolean replaceAll = flags.contains("g");
        final boolean reportMatches = flags.contains("n");
        final boolean caseSensitive = isCaseSensitive(toFind, flags);

        int numReplaces = 0;
        toFind = convertRegexSearch(toFind);
        IRegion result;
        try {
            result = adapter.find(start, toFind, true, caseSensitive, false, true);
            if (result != null && result.getOffset() < end) {
                numReplaces++;
                if ( ! reportMatches) {
                    IRegion replacedresult = adapter.replace(replace, true);
                    // Fix end position when line gets longer or shorter
                    end = end + (replacedresult.getLength() - result.getLength());
                    result = replacedresult;
                }
                result = adapter.find(result.getOffset() + result.getLength(),
                        toFind, true, caseSensitive, false, true);
            }
            while (replaceAll && result != null && result.getOffset() < end) {
                numReplaces++;
                if ( ! reportMatches) {
                    IRegion replacedresult = adapter.replace(replace, true);
                    end = end + (replacedresult.getLength() - result.getLength());
                    result = replacedresult;
                }
                result = adapter.find(result.getOffset() + result.getLength(),
                        toFind, true, caseSensitive, false, true);
            }
        } catch (BadLocationException e) {
            VrapperLog.error("Failed to replace for " + line, e);
        }
        
        return numReplaces;
    }

    public boolean isCaseSensitive(String toFind, String flags) {
        boolean caseSensitive = !configuration.get(Options.IGNORE_CASE)
            || (configuration.get(Options.SMART_CASE)
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
            toFind = convertRegexSearch(toFind);
            IRegion result = adapter.find(start, toFind, true, isCaseSensitive(toFind, flags), false, true);
            if(result != null) {
                adapter.replace(toReplace, true);
                success = true;
            }
        } catch (BadLocationException e) {
            VrapperLog.error("Failed to substitute starting from M" + start, e);
        }
        return success;
    }

    private IRegion find(Search search, int begin) throws BadLocationException {
        if(search.isRegExSearch()) {
            search = convertRegexSearch(search);
        }
        IRegion result;
        try {
            result = adapter.find(
                        begin, search.getKeyword(),
                        !search.isBackward(), search.isCaseSensitive(),
                        search.isWholeWord(), search.isRegExSearch());
        } catch (BadLocationException e) {
            throw new VrapperPlatformException("Failed to find '" + search.getKeyword() + "' at "
                    + "offset" + begin + ", offset is invalid.", e);
        } catch (PatternSyntaxException e) {
            throw new VrapperPlatformException("Failed to find '" + search.getKeyword() + "' at "
                    + "offset" + begin + ", search pattern is invalid.", e);
        }
        return result;
    }
    
    /**
     * We're using Eclipse's (Java's) Regex engine for search/replace. However,
     * a lot of people are used to Vim's syntax for regex. So, check for Vim
     * regex syntax and map to Eclipse's (Java's) syntax where possible.
     * \< and \> =  \b   = word boundary
     * (Feature pending... add support for 'magic' and 'very magic')
     */
    private Search convertRegexSearch(Search search) {
        String keyword = search.getKeyword();
        keyword = convertRegexSearch(keyword);
        return new Search(keyword, search.isBackward(), search.isWholeWord(),
            search.isCaseSensitive(), search.getSearchOffset(), search.isRegExSearch());
    }
    private String convertRegexSearch(String keyword) {
        //in Vim, '\<' and '\>' are word boundaries
        //in Eclipse, '\b' is word boundaries
        return keyword.replaceAll("\\\\<|\\\\>", "\\\\b");
    }

    public void removeHighlighting() {
        lastHighlightedSearch = null;
        highlightingService.removeHighlights(annotations);
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
            List<TextRange> rangesToHL = new ArrayList<TextRange>();
            TextViewerPosition temp = new TextViewerPosition(textViewer, Space.MODEL, 0);
            while ((result = find(search, result.getOffset()+result.getLength())) != null) {
                Position start = temp.setModelOffset(result.getOffset());
                Position end = temp.setModelOffset(result.getOffset() + result.getLength());
                rangesToHL.add(StartEndTextRange.exclusive(start, end));
            }
            annotations = highlightingService.highlightRegions(ANNOTATION_TYPE,
                    "Vrapper Search", rangesToHL);
        } catch (BadLocationException e) {
            VrapperLog.error("while highlighting search", e);
        }
    }

    public void incSearchhighlight(Position start, int length) {
        removeIncSearchHighlighting();
        incSearchAnnotation = highlightingService.highlightRegion(INC_ANNOTATION_TYPE,
                "Incremental Search", start.getModelOffset(), length);
    }

    public void removeIncSearchHighlighting() {
        if (incSearchAnnotation != null) {
            highlightingService.removeHighlighting(incSearchAnnotation);
        }
    }

}
