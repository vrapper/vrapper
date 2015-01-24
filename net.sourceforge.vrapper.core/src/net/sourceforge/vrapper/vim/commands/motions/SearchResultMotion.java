package net.sourceforge.vrapper.vim.commands.motions;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset.Begin;
import net.sourceforge.vrapper.utils.SearchOffset.End;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class SearchResultMotion extends CountAwareMotion {

    public static final SearchResultMotion REPEAT = new SearchResultMotion(false);
    public static final SearchResultMotion REPEAT_REVERSED = new SearchResultMotion(true);

    public static final Motion PREVIOUS_BEGIN = new SearchResultMotion(new Begin(0));
    public static final Motion NEXT_END = new SearchResultMotion(new End(0));

    public static final TextObject SELECT_NEXT_MATCH = new SearchResultTextObject(false);
    public static final TextObject SELECT_PREVIOUS_MATCH = new SearchResultTextObject(true);
    
    private static final String NOT_FOUND_MESSAGE = "'%s' not found";

    protected final boolean reverse;
    private Boolean forcedBackwards;
    private boolean includesTarget;
    private boolean lineWise;
    private SearchOffset fixedOffset;

    protected SearchResultMotion(boolean reverse) {
        super();
        this.reverse = reverse;
    }

    public SearchResultMotion(SearchOffset offset) {
        this.fixedOffset = offset;
        this.forcedBackwards = (offset instanceof Begin);
        reverse = false;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        Search search = editorAdaptor.getRegisterManager().getSearch();
        if (search == null) {
            throw new CommandExecutionException("no search string given");
        }
        if(editorAdaptor.getConfiguration().get(Options.SEARCH_REGEX)) {
            //before attempting search, is this regex even valid?
            try {
                Pattern.compile(search.getKeyword());
            }
            catch(PatternSyntaxException e) {
                throw new CommandExecutionException("Invalid regex search string: " + search.getKeyword());
            }
        }
        SearchOffset offset = (fixedOffset == null ? search.getSearchOffset() : fixedOffset);
        SearchResult result = editorAdaptor.getRegisterManager().getLastSearchResult();
        TextContent modelContent = editorAdaptor.getModelContent();

        boolean shouldReverse = reverse;
        if (forcedBackwards != null) {
            shouldReverse = (search.isBackward() != forcedBackwards);
        }

        includesTarget = offset instanceof End;
        lineWise = offset.lineWise();

        Position position;
        if (result == null || ! result.isFound()) {
            position = editorAdaptor.getPosition();
        } else {
            position = offset.unapply(modelContent, editorAdaptor.getPosition(), result);
        }
        for (int i = 0; i < count; i++) {
            result = doSearch(search, shouldReverse, editorAdaptor, position);
            editorAdaptor.getRegisterManager().setLastSearchResult(result);
            if (! result.isFound()) {
                editorAdaptor.getSearchAndReplaceService().removeHighlighting();
                throw new CommandExecutionException(
                        String.format(NOT_FOUND_MESSAGE, search.getKeyword()));
            }
            position = result.getStart();
        }
        if (editorAdaptor.getConfiguration().get(Options.SEARCH_HIGHLIGHT)) {
            editorAdaptor.getSearchAndReplaceService().highlight(search);
        }
        return offset.apply(modelContent, result);
    }

    public BorderPolicy borderPolicy() {
        if (lineWise) {
            return BorderPolicy.LINE_WISE;
        }
        if (includesTarget) {
            return BorderPolicy.INCLUSIVE;
        }
        return BorderPolicy.EXCLUSIVE;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    protected static SearchResult doSearch(Search search, boolean reverse, EditorAdaptor vim,
            Position position) {
        if (reverse) {
            search = search.reverse();
        }
        if (!search.isBackward()) {
            position = position.addModelOffset(1);
        } else if (search.getKeyword().length() == 1) {
            if (position.getModelOffset() > 0) {
                position = position.addModelOffset(-1);
            }
        }
        return VimUtils.wrapAroundSearch(vim, search, position);
    }

    @Override
    public boolean isJump() {
        return true;
    }

    public static class SearchResultTextObject extends AbstractTextObject {
        
        protected final boolean backwards;
        
        protected SearchResultTextObject(boolean backward) {
            this.backwards = backward;
        }

        @Override
        public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            if (count == NO_COUNT_GIVEN) {
                count = 1;
            }
            Search search = editorAdaptor.getRegisterManager().getSearch();
            if (search == null) {
                throw new CommandExecutionException("no search string given");
            }
            if (search.isBackward() != backwards) {
                search = search.reverse();
            }
            SearchResult nextMatch = getCurrentMatch(editorAdaptor, search);
            Position position;
            if (nextMatch == null) {
                position = editorAdaptor.getPosition();
            } else {
                position = nextMatch.getLeftBound();
                count--;
            }
            for (int i = 0; i < count; i++) {
                nextMatch = doSearch(search, false, editorAdaptor, position);
                if ( ! nextMatch.isFound()) {
                    editorAdaptor.getSearchAndReplaceService().removeHighlighting();
                    throw new CommandExecutionException(
                            String.format(NOT_FOUND_MESSAGE, search.getKeyword()));
                }
                position = nextMatch.getEnd();
            }
            Position start = nextMatch.getStart();
            Position end = nextMatch.getEnd();
            // Flip selection, though this only matters when SelectTextObjectCommand is calling us.
            if (backwards) {
                start = nextMatch.getEnd();
                end = nextMatch.getStart();
            }
            TextRange result;
            result = new StartEndTextRange(start, end);
            return result;
        }

        /**
         * Checks if the cursor is inside a match in which case we should select this first.
         */
        protected SearchResult getCurrentMatch(EditorAdaptor editorAdaptor,
                Search search) {
            SearchResult currentMatch = null;
            Search tempSearch = search;
            if ( ! search.isBackward()) {
                tempSearch = search.reverse();
            }
            // Search backwards but allow to hit the current position.
            SearchAndReplaceService searchService = editorAdaptor.getSearchAndReplaceService();
            Position position = editorAdaptor.getPosition();
            SearchResult testMatch = searchService.find(tempSearch, position);
            int currentOffset = position.getModelOffset();
            if (testMatch.isFound()
                    && testMatch.getLeftBound().getModelOffset() <= currentOffset
                    && testMatch.getRightBound().getModelOffset() > currentOffset) {
                currentMatch = testMatch;
            } else {
                // Sometimes the Search Service skips the current match if we're in the middle.
                // Move 'position' to the left and search from there to the right this time.
                tempSearch = tempSearch.reverse();
                if (testMatch.isFound()
                    && testMatch.getRightBound().getModelOffset() <= currentOffset) {
                    position = testMatch.getRightBound();
                } else {
                    position = position.setModelOffset(0);
                }
                testMatch = searchService.find(tempSearch, position);
                if (testMatch.isFound()
                        && testMatch.getLeftBound().getModelOffset() <= currentOffset
                        && testMatch.getRightBound().getModelOffset() > currentOffset) {
                    currentMatch = testMatch;
                }
            }
            return currentMatch;
        }

        @Override
        public ContentType getContentType(Configuration configuration) {
            return ContentType.TEXT;
        }
        
    }
}
