package net.sourceforge.vrapper.core.tests.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;

public class TestSearchService implements SearchAndReplaceService {
    
    private final TestTextContent content;

    public TestSearchService(TestTextContent content) {
        this.content = content;
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

    /** Regex replace, case-sensitive. */
    public int replace(LineInformation line, String toFind, String replace, String flags) {
//        String stack = content.getText().substring(line.getBeginOffset(), line.getLength());
//        if (stack.contains(toFind)) {
//            if (flags.contains("g")) {
//                stack = stack.replaceAll(toFind, replace);
//            } else {
//                stack = stack.replaceFirst(toFind, replace);
//            }
//            content.replace(line.getBeginOffset(), line.getLength(), stack);
//            return true;
//        }
//        return false;
        throw new UnsupportedOperationException();
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
