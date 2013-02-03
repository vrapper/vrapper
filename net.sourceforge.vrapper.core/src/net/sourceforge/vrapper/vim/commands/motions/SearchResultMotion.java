package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset.End;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class SearchResultMotion extends CountAwareMotion {

    public static final SearchResultMotion FORWARD = new SearchResultMotion(false);
    public static final SearchResultMotion BACKWARD = new SearchResultMotion(true);
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
        includesTarget = search.getSearchOffset() instanceof End;
        lineWise = search.getSearchOffset().lineWise();
        Position position = search.getSearchOffset().unapply(
                editorAdaptor, editorAdaptor.getPosition());
        for (int i = 0; i < count; i++) {
            position = doSearch(search, editorAdaptor, position);
            if (position == null) {
                editorAdaptor.getSearchAndReplaceService().removeHighlighting();
                throw new CommandExecutionException(
                        String.format(NOT_FOUND_MESSAGE, search.getKeyword()));
            }
        }
        if (editorAdaptor.getConfiguration().get(Options.SEARCH_HIGHLIGHT)) {
            editorAdaptor.getSearchAndReplaceService().highlight(search);
        }
        return search.getSearchOffset().apply(editorAdaptor, position);
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

    public boolean updateStickyColumn() {
        return true;
    }

    private Position doSearch(Search search, EditorAdaptor vim, Position position) {
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
        return VimUtils.wrapAroundSearch(vim, search, position).getStart();
    }

    @Override
    public boolean isJump() {
        return true;
    }

}
