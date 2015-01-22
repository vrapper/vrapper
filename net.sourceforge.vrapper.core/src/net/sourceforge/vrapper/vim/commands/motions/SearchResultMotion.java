package net.sourceforge.vrapper.vim.commands.motions;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.SearchOffset.End;
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

    public static final TextObject SELECT_NEXT_MATCH = new SearchResultTextObject(false);
    public static final TextObject SELECT_PREVIOUS_MATCH = new SearchResultTextObject(true);
    
    private static final String NOT_FOUND_MESSAGE = "'%s' not found";

    protected final boolean reverse;
    private boolean includesTarget;
    private boolean lineWise;

    protected SearchResultMotion(boolean reverse) {
        super();
        this.reverse = reverse;
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

        includesTarget = search.getSearchOffset() instanceof End;
        lineWise = search.getSearchOffset().lineWise();

        SearchResult result = editorAdaptor.getRegisterManager().getLastSearchResult();
        Position position;
        if (result == null || ! result.isFound()) {
            position = editorAdaptor.getPosition();
        } else {
            position = search.getSearchOffset().unapply(
                editorAdaptor.getModelContent(), editorAdaptor.getPosition(), result);
        }
        for (int i = 0; i < count; i++) {
            result = doSearch(search, reverse, editorAdaptor, position);
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
        return search.getSearchOffset().apply(editorAdaptor.getModelContent(), result);
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
            Position position = editorAdaptor.getPosition();
            SearchResult nextMatch = null;
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
            // Flip selection
            if (backwards) {
                start = nextMatch.getEnd();
                end = nextMatch.getStart();
            }
            TextRange result;
            result = new StartEndTextRange(start, end);
            return result;
        }

        @Override
        public ContentType getContentType(Configuration configuration) {
            return ContentType.TEXT;
        }
        
    }
}
