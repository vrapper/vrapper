package net.sourceforge.vrapper.plugin.sneak.commands.motions;

import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakState;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakStateManager;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;
import net.sourceforge.vrapper.vim.commands.motions.NavigatingMotion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class SneakMotion extends CountAwareMotion implements NavigatingMotion {

    private SneakStateManager stateManager;

    private int columnLeft;
    /** Right boundary for sneak-column mode. */
    private int columnRight;
    private Search searchKeyword;

    public SneakMotion(SneakStateManager sneakStateManager, Search searchKeyword) {
        this(sneakStateManager, searchKeyword, -1, -1);
    }

    public SneakMotion(SneakStateManager sneakStateManager, Search searchKeyword, int columnLeft,
            int columnRight) {
        this.stateManager = sneakStateManager;
        this.searchKeyword = searchKeyword;
        this.columnLeft = columnLeft;
        this.columnRight = columnRight;
    }

    @Override
    public boolean isBackward() {
        return searchKeyword.isBackward();
    }

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {

        Position result = null;

        if (count <= NO_COUNT_GIVEN) {
            count = 1;
        }

        SneakState state = stateManager.getSneakState(editorAdaptor);

        stateManager.markSneakActive(editorAdaptor);

        if (state.isSearchStateUsable(searchKeyword, columnLeft, columnRight)) {

            if (state.isPreviousMatchStateOnSameKeywordAndDirection(searchKeyword)) {
                result = state.sneakToNextMatch(editorAdaptor, count);
            } else {
                state.reverseSearchDirection(editorAdaptor);
                result = state.sneakToNextMatch(editorAdaptor, count);
            }
        } else {
            state.deactivateSneak(editorAdaptor.getHighlightingService());
            state.runNewSearch(editorAdaptor, count, searchKeyword, columnLeft, columnRight);
            result = state.sneakToNextMatch(editorAdaptor, count);
        }


        return result;
    }

    @Override
    public SneakMotion reverse() {
        return new SneakMotion(stateManager, searchKeyword.reverse(), columnLeft, columnRight);
    }

    @Override
    public NavigatingMotion repetition() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> type) {
        if (NavigatingMotion.class.equals(type)) {
            return (T) this;
        } else if (SneakMotion.class.equals(type)) {
            return (T) this;
        }
        return null;
    }
}
