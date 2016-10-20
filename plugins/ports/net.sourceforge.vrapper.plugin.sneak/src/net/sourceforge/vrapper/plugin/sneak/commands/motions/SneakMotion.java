package net.sourceforge.vrapper.plugin.sneak.commands.motions;

import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakCharOffset;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakState;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakStateManager;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.TextRange;
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
    /** Offset to correct jumps when using the f/F/t/T alternatives. */
    private SneakCharOffset searchOffset;

    public SneakMotion(SneakStateManager sneakStateManager, Search searchKeyword, int columnLeft,
            int columnRight, SneakCharOffset searchOffset) {
        this.stateManager = sneakStateManager;
        this.searchKeyword = searchKeyword;
        this.columnLeft = columnLeft;
        this.columnRight = columnRight;
        this.searchOffset = searchOffset;
    }

    @Override
    public boolean isBackward() {
        return searchKeyword.isBackward();
    }

    @Override
    public BorderPolicy borderPolicy() {
        return isFfTtMode() ? BorderPolicy.INCLUSIVE : BorderPolicy.EXCLUSIVE;
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {

        TextRange match = null;

        if (count <= NO_COUNT_GIVEN) {
            count = 1;
        }

        SneakState state = stateManager.getSneakState(editorAdaptor);

        stateManager.markSneakActive(editorAdaptor);

        if (state.isSearchStateUsable(searchKeyword, columnLeft, columnRight)) {

            if (state.isPreviousMatchStateOnSameKeywordAndDirection(searchKeyword)) {
                match = state.sneakToNextMatch(editorAdaptor, count);
            } else {
                state.reverseSearchDirection(editorAdaptor);
                match = state.sneakToNextMatch(editorAdaptor, count);
            }
        } else {
            state.deactivateSneak(editorAdaptor.getHighlightingService());
            state.runNewSearch(editorAdaptor, count, searchKeyword, columnLeft, columnRight);
            match = state.sneakToNextMatch(editorAdaptor, count);
        }

        Position result;
        if (searchOffset == null) {
            result = match.getLeftBound();
        } else {
            result = searchOffset.apply(editorAdaptor, match);
        }

        return result;
    }

    @Override
    public SneakMotion reverse() {

        SneakCharOffset reversedOffset = searchOffset;
        if (searchOffset != null) {
            reversedOffset = searchOffset.reverse();
        }

        return new SneakMotion(stateManager, searchKeyword.reverse(), columnLeft, columnRight,
                reversedOffset);
    }

    /**
     * @return <code>true</code> when we're dealing with sneak_f/F/t/T.
     */
    private boolean isFfTtMode() {
        return searchOffset != null;
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
