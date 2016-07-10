package net.sourceforge.vrapper.plugin.sneak.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakState;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakStateManager;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakInputMode;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class SneakTextObject extends AbstractTextObject {

    /** Cached search keyword so that . */
    private Search searchKeyword;
    private SneakStateManager stateManager;
    private String searchString;
    private boolean sneakBackwards;

    /**
     * Create a sneak text object.
     * @param sneakStateManager Injects a particular type of {@link SneakStateManager}.
     * @param searchString the search keyword. This is passed in as a string rather than a Search
     *     instance because the Vrapper keybind state machine hasn't got access to user settings
     *     like 'ignorecase' or the last sneak keyword.
     */
    public SneakTextObject(SneakStateManager sneakStateManager, String searchString,
            boolean sneakBackwards) {
        this.stateManager = sneakStateManager;
        this.searchString = searchString;
        this.sneakBackwards = sneakBackwards;
        if (searchString == null) {
            throw new NullPointerException("Search string parameter cannot be null.");
        }
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        Position initialPosition = editorAdaptor.getPosition();
        if (searchKeyword == null) {
            if (searchString.isEmpty()) {
                SneakState sneakState;
                try {
                    sneakState = stateManager.getSneakState(editorAdaptor);
                } catch (VrapperPlatformException e) {
                    // No state present?!?
                    return new StartEndTextRange(initialPosition, initialPosition);
                }
                searchKeyword = sneakState.getSneakSearch();
            } else {
                searchKeyword = SneakInputMode.constructSearchKeyword(editorAdaptor, searchString,
                        sneakBackwards);
            }
        }
        CursorService cursorService = editorAdaptor.getCursorService();
        SearchAndReplaceService searchAndReplaceService = editorAdaptor.getSearchAndReplaceService();

        Position result = null;

        // Move position so we don't hit current match again. Shifting clips to file boundaries.
        Position position = initialPosition;
        int offsetShift = (searchKeyword.isBackward() ? -1 : 1);
        position = cursorService.shiftPositionForModelOffset(position.getModelOffset(),
                offsetShift, true);

        SearchResult searchResult = searchAndReplaceService.find(searchKeyword, position);

        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        int nFound = 0;
        while (searchResult.isFound() && nFound < count) {
            result = searchResult.getLeftBound();
            // Shift and find again
            position = cursorService.shiftPositionForModelOffset(
                    searchResult.getLeftBound().getModelOffset(), offsetShift, true);
            searchResult = searchAndReplaceService.find(searchKeyword, position);
            nFound++;
        }
        if (result == null) {
            return new StartEndTextRange(initialPosition, initialPosition);
        }

        // This find is exclusive border type so we don't need any extra work
        return new StartEndTextRange(initialPosition, result);
    }

    @Override
    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }
}
