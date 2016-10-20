package net.sourceforge.vrapper.plugin.sneak.commands.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.plugin.sneak.commands.motions.SneakMotion;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VrapperEventListener;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.register.RegisterManager;

/**
 * Stores information about sneak's status in a given editor.
 * This has nothing to do with Vrapper keymap {@link State}s.
 */
public class SneakState {

    /** Max size for the nextMatches or previousMatches lists (separately). */
    private static final int MAX_SNEAK_CACHE_SIZE = 500;

    /** Minimal number of items which should be in the cache. */
    private static final int MIN_SNEAK_CACHE_PREFILL = 250;

    private static final String SNEAK_HIGHLIGHT_TYPE = "net.sourceforge.vrapper.eclipse.incsearchhighlight";

    private static final String NOT_FOUND_MESSAGE = "'%s' not found";

    /**
     * This indicates whether the previousMatches list contains all possible results or whether
     * we need to fill the buffer for more.
     */
    private boolean allPreviousMatchesFound;
    /**
     * This indicates whether the nextMatches list contains all possible results or whether
     * we need to fill the buffer for more.
     */
    private boolean allNextMatchesFound;

    /** Left boundary for sneak-column mode. */
    private int columnLeft;
    /** Right boundary for sneak-column mode. */
    private int columnRight;
    private LinkedList<Object> nextHighlights;
    private LinkedList<TextRange> nextMatches;
    private LinkedList<TextRange> previousMatches;

    private Search sneakSearch;

    /*
     * Shared with SneakStateManager
     */
    boolean isSneaking;
    long globalEventCounter;
    long lastSneakCommandEventCounter;

    /**
     * Default constructor, marking sneak inactive.
     */
    public SneakState() {
        isSneaking = false;
        lastSneakCommandEventCounter = -1;
        columnLeft = -1;
        columnRight = -1;
        nextHighlights = new LinkedList<Object>();
        nextMatches = new LinkedList<TextRange>();
        previousMatches = new LinkedList<TextRange>();
    }

    /**
     * Retrieves the search keyword for which the current cache data is calculated. The cache should
     * be cleared every time this keyword is changed, either through SneakInputMode or through a
     * different editor putting a fresh {@link SneakMotion} into the {@link RegisterManager}.
     * @return the last sneak search. Can be <code>null</code> if {@link #isSneaking()} returns
     * <code>false</code>.
     */
    public Search getSneakSearch() {
        return sneakSearch;
    }

    /**
     * This event counter is used to keep track of how often the 
     * {@link VrapperEventListener#commandAboutToExecute(net.sourceforge.vrapper.vim.modes.EditorMode)}
     * function has been called in the current editor.
     */
    public long getGlobalEventCounter() {
        return globalEventCounter;
    }

    /**
     * This counter tells us if the last event on {@link VrapperEventListener} got handled by sneak.
     * It is the {@link SneakStateManager}'s responsibility to sync it with the the global event
     * counter when
     * {@link SneakStateManager#markSneakActive(net.sourceforge.vrapper.vim.EditorAdaptor, boolean, List)}
     * is called.
     */
    public long getLastSneakCommandEventCounter() {
        return lastSneakCommandEventCounter;
    }

    /**
     * Whether the last command was also a sneak command. Useful to know if the highlighting can be
     * just slightly tweaked or if it needs to be recalculated from scratch.
     */
    public boolean isSneaking() {
        return isSneaking;
    }

    private boolean hasHighlightsActive() {
        return ! nextHighlights.isEmpty();
    }

    public void deactivateSneak(HighlightingService highlightingService) {
        if (hasHighlightsActive()) {
            clearHighlights(highlightingService, 0, nextHighlights.size());
        }
        isSneaking = false;
        nextMatches.clear();
        previousMatches.clear();
        allPreviousMatchesFound = false;
        allNextMatchesFound = false;
    }

    public void keepSneakActive() {
        lastSneakCommandEventCounter = globalEventCounter;
    }

    protected void clearHighlights(HighlightingService highlightingService,
            int startindex, int numberOfItems) {
        List<Object> highlightsToRemove;
        if (startindex == 0 && numberOfItems >= nextHighlights.size()) {
            highlightsToRemove = nextHighlights;
        } else {
           highlightsToRemove = nextHighlights.subList(startindex, numberOfItems);
        }

        highlightingService.removeHighlights(highlightsToRemove);
        highlightsToRemove.clear();
    }

    /**
     * Whether the previous search results can be reused, which is only the case if the sneak
     * parameters haven't changed and the editor hasn't been modified.
     */
    public boolean isSearchStateUsable(Search searchKeyword, int columnLeft, int columnRight) {
        return isSneaking
                && searchKeyword.equalsIgnoreDirection(this.sneakSearch)
                && columnLeft == this.columnLeft
                && columnRight == this.columnRight;
    }

    public boolean isPreviousMatchStateOnSameKeywordAndDirection(Search searchKeyword) {
        return searchKeyword.equals(this.sneakSearch);
    }

    public void reverseSearchDirection(EditorAdaptor editorAdaptor) {

        HighlightingService highlightingService = editorAdaptor.getHighlightingService();
        clearHighlights(highlightingService, 0, nextHighlights.size());

        sneakSearch = sneakSearch.reverse();

        // Never stick around on the same spot when reversing (the first "previous match" is where
        // we landed last time) by immediately transferring that match to the other list.
        transferCurrentMatchToFuturePreviousMatches(editorAdaptor.getPosition());

        LinkedList<TextRange> tempForReversal = previousMatches;
        previousMatches = nextMatches;
        nextMatches = tempForReversal;

        boolean tempForBooleanReversal = allPreviousMatchesFound;
        allPreviousMatchesFound = allNextMatchesFound;
        allNextMatchesFound = tempForBooleanReversal;

        highlightAdditionalMatches(nextMatches, highlightingService);
    }

    private void transferCurrentMatchToFuturePreviousMatches(Position currentPosition) {

        if (previousMatches.size() > 0) {
            Position lastDestination = previousMatches.getFirst().getLeftBound();
            int currentPositionOffset = currentPosition.getModelOffset();
            int distanceToLastDestination = currentPositionOffset - lastDestination.getModelOffset();

            // Tolerate a slight difference, the f/t motions might have shifted the current position
            if (Math.abs(distanceToLastDestination) <= 1) {
                // Add it to next matches list, the next and previous lists will be swapped later
                // so we don't need to modify any highlights on this spot
                nextMatches.addFirst(previousMatches.remove());
            }
        } // else there is no current match
    }

    public void runNewSearch(EditorAdaptor editorAdaptor, int count, Search searchKeyword,
            int columnLeft, int columnRight) {

        HighlightingService highlightingService = editorAdaptor.getHighlightingService();
        deactivateSneak(highlightingService);

        this.sneakSearch = searchKeyword;
        this.columnLeft = columnLeft;
        this.columnRight = columnRight;

        findNextMatches(editorAdaptor, editorAdaptor.getPosition(), MIN_SNEAK_CACHE_PREFILL + count);
    }

    private void findNextMatches(EditorAdaptor editorAdaptor, Position startPosition,
            int additionalItemCount) {
        if (allNextMatchesFound) {
            return;
        }

        int offsetShift = (sneakSearch.isBackward() ? -1 : 1);
        SearchAndReplaceService searchAndReplaceService = editorAdaptor.getSearchAndReplaceService();
        CursorService cursorService = editorAdaptor.getCursorService();

        List<TextRange> searchHits = new ArrayList<TextRange>();

        // Move position so we don't hit current match again. Shifting clips to file boundaries.
        Position position;
        position = cursorService.shiftPositionForModelOffset(startPosition.getModelOffset(),
                offsetShift, true);

        SearchResult searchResult = searchAndReplaceService.find(sneakSearch, position);
        ColumnFilter columnFilter = new ColumnFilter(editorAdaptor, columnLeft, columnRight);

        int countResults = 0;
        while (searchResult.isFound() && countResults < additionalItemCount) {
            if (columnFilter.considerMatch(searchResult)) {
                searchHits.add(searchResult);
                countResults++;
            }
            position = cursorService.shiftPositionForModelOffset(
                    searchResult.getLeftBound().getModelOffset(), offsetShift, true);
            searchResult = searchAndReplaceService.find(sneakSearch, position);
        }
        storeAdditionalMatches(searchResult, searchHits);
        highlightAdditionalMatches(searchHits, editorAdaptor.getHighlightingService());
    }

    private void storeAdditionalMatches(SearchResult lastSearchResult,
            List<TextRange> additionalMatches) {
        if ( ! lastSearchResult.isFound()) {
            allNextMatchesFound = true;
        }
        nextMatches.addAll(additionalMatches);
    }

    private void highlightAdditionalMatches(List<TextRange> searchHits,
            HighlightingService highlightingService) {
        List<Object> highlights;
        highlights = highlightingService.highlightRegions(SNEAK_HIGHLIGHT_TYPE, "Sneak hit", searchHits);
        nextHighlights.addAll(highlights);
    }

    public TextRange sneakToNextMatch(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {

        prepareNextMatches(editorAdaptor, count);

        if (nextMatches.size() == 0) {
            throw new CommandExecutionException(String.format(NOT_FOUND_MESSAGE,
                    sneakSearch.getKeyword()));
        }
        if (nextMatches.size() < count) {
            // Simply jump to last possible match
            count = nextMatches.size();
        }
        TextRange result = nextMatches.get(count - 1);
        transferNextMatchesToPreviousMatches(editorAdaptor, count);

        isSneaking = true;

        trimNextMatchesCache(editorAdaptor);

        return result;
    }

    private void prepareNextMatches(EditorAdaptor editorAdaptor, int count) {
        // Add <count> new entries to nextMatches to keep it filled
        if ( ! allNextMatchesFound) {
            TextRange lastMatch = nextMatches.peekLast();
            Position searchStartPosition;
            // nextMatches may be empty
            if (lastMatch == null) {
                searchStartPosition = editorAdaptor.getPosition();
            } else {
                searchStartPosition = lastMatch.getLeftBound();
            }
            int prefillFetchCount = MIN_SNEAK_CACHE_PREFILL - nextMatches.size() + count;
            int itemsToAddCount = Math.max(count, prefillFetchCount);
            findNextMatches(editorAdaptor, searchStartPosition, itemsToAddCount);
        }
    }

    private void transferNextMatchesToPreviousMatches(EditorAdaptor editorAdaptor, int count) {
        // These matches need to be moved in reversed order so we can't use subList
        for (int i = 0; i < count; i++) {
            previousMatches.addFirst(nextMatches.remove());
        }
        clearHighlights(editorAdaptor.getHighlightingService(), 0, count);
    }

    private void trimNextMatchesCache(EditorAdaptor editorAdaptor) {
        // This can happen when we just did a jump like 500;
        if (nextMatches.size() > MAX_SNEAK_CACHE_SIZE) {
            List<TextRange> itemsToTrim = nextMatches.subList(MAX_SNEAK_CACHE_SIZE, nextMatches.size());
            List<Object> highlightsToTrim = nextHighlights.subList(MAX_SNEAK_CACHE_SIZE, nextHighlights.size());
            editorAdaptor.getHighlightingService().removeHighlights(highlightsToTrim);
            highlightsToTrim.clear();
            itemsToTrim.clear();

            allNextMatchesFound = false;
        }
        if (previousMatches.size() > MAX_SNEAK_CACHE_SIZE) {
            List<TextRange> itemsToTrim = previousMatches.subList(MAX_SNEAK_CACHE_SIZE, previousMatches.size());
            itemsToTrim.clear();
        }
    }

    protected static class ColumnFilter {
        protected EditorAdaptor editorAdaptor;
        protected TextContent textContent;
        private int columnLeft;
        private int columnRight;
        private Integer tabstopSetting;
        protected LineInformation lastLineInfo;
        protected int[] lastLineOffsets;

        public ColumnFilter(EditorAdaptor editorAdaptor, int columnLeft, int columnRight) {
            this.editorAdaptor = editorAdaptor;
            this.columnRight = columnRight;
            this.columnLeft = columnLeft;
            textContent = editorAdaptor.getModelContent();
            tabstopSetting = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
        }

        public boolean considerMatch(TextRange match) {
            if (columnLeft == -1 || columnRight == -1) {
                return true;
            }

            int matchOffset = match.getLeftBound().getModelOffset();
            LineInformation matchLineInfo = textContent.getLineInformationOfOffset(matchOffset);

            if (lastLineInfo == null || lastLineInfo.getNumber() != matchLineInfo.getNumber()) {
                String currentLineContent = textContent.getText(matchLineInfo.getBeginOffset(),
                        matchLineInfo.getLength());
                lastLineInfo = matchLineInfo;
                lastLineOffsets = StringUtils.calculateVisualOffsets(currentLineContent,
                        currentLineContent.length(), tabstopSetting);
            } // else the last line is the same as the current line, reuse info

            int currentOffsetInLine = matchOffset - lastLineInfo.getBeginOffset();
            int currentColumn = lastLineOffsets[currentOffsetInLine];
            return currentColumn >= columnLeft && currentColumn <= columnRight;
        }
    }
}
