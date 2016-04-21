package net.sourceforge.vrapper.core.tests.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.Options;

public class TestSearchService implements SearchAndReplaceService {
    
    private final TestTextContent content;
    private Configuration sharedConfiguration;

    public TestSearchService(TestTextContent content, Configuration sharedConfiguration) {
        this.content = content;
        this.sharedConfiguration = sharedConfiguration;
    }

    /** Case-sensitive search only. */
    public SearchResult find(Search search, Position start) {
        String stack = content.getText();
        String needle = search.getKeyword();
        int patternFlags = 0;
        if ( ! search.isRegExSearch()) {
            patternFlags = Pattern.LITERAL;
        }
        if ( ! search.isCaseSensitive()) {
            patternFlags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern pattern = Pattern.compile(needle, patternFlags);
        Matcher matcher = pattern.matcher(stack);
        
        Position resultPosition = null;
        Position endPosition = null;
        if (search.isBackward()) {
            matcher.region(0, start.getModelOffset());
            // Find last match by looping from the start until no more match can be found
            while (matcher.find()) {
                resultPosition =  start.setModelOffset(matcher.start());
                endPosition =  start.setModelOffset(matcher.end());
            }
        } else if (matcher.find(start.getModelOffset())) {
            resultPosition =  start.setModelOffset(matcher.start());
            endPosition =  start.setModelOffset(matcher.end());
        }
        SearchResult result = new SearchResult(resultPosition, endPosition);
        return result;
    }

    /**
     * Test replace stub.
     */
    public int replace(int start, int end, String toFind, String replace, String flags) {
        int nMatches = 0;
        int length = end - start;

        StringBuilder result = new StringBuilder();
        String lineContent = content.getText().substring(start, length);
        int patternFlags = 0;
        boolean doReplace = ! flags.contains("n");
        boolean allMatches = flags.contains("g");
        if ( ! isCaseSensitive(toFind, flags)) {
            patternFlags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern pattern = Pattern.compile(toFind, patternFlags);
        Matcher matcher = pattern.matcher(lineContent);

        int lastMatchEnd = 0;
        if (matcher.find()) {
            nMatches++;
            result.append(lineContent.substring(lastMatchEnd, matcher.start()));
            result.append(replace);
            lastMatchEnd = matcher.end();
        }
        while (allMatches && matcher.find()) {
            nMatches++;
            result.append(lineContent.substring(lastMatchEnd, matcher.start()));
            result.append(replace);
            lastMatchEnd = matcher.end();
        }
        result.append(lineContent.substring(lastMatchEnd, lineContent.length()));
        if (doReplace) {
            content.replace(start, length, result.toString());
        }
        return nMatches;
    }

	public boolean isCaseSensitive(String toFind, String flags) {
        boolean caseSensitive = !sharedConfiguration.get(Options.IGNORE_CASE)
            || (sharedConfiguration.get(Options.SMART_CASE)
                && StringUtils.containsUppercase(toFind));
        if (flags.contains("i"))
            caseSensitive = false;
        if (flags.contains("I"))
            caseSensitive = true;
        return caseSensitive;
	}

    /** Does nothing. */
    public void highlight(Search search) {
    }

    /** Does nothing. */
    public void removeHighlighting() {
    }

    /** Does nothing. */
    public void incSearchhighlight(Position start, int length) {
    }

    /** Does nothing. */
    public void removeIncSearchHighlighting() {
    }

    @Override
    public boolean substitute(int start, String toFind, String flags, String toReplace) {
        throw new UnsupportedOperationException();
    }

}
