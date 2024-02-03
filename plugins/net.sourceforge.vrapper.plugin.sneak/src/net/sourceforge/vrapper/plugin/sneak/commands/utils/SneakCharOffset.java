package net.sourceforge.vrapper.plugin.sneak.commands.utils;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Defines an additional char offset to simulate t/T in vim-sneak.
 */
public class SneakCharOffset {
    public static final SneakCharOffset NONE = new SneakCharOffset(0);
    private final int offset;

    public SneakCharOffset(int offset) {
        this.offset = offset;
    }

    public Position apply(EditorAdaptor editorAdaptor, TextRange sneakMatch) {
        if (offset == 0) {
            return sneakMatch.getLeftBound();
        }
        Position start = sneakMatch.getLeftBound();
        return editorAdaptor.getCursorService().shiftPositionForModelOffset(start.getModelOffset(),
                offset, true);
    }

    public SneakCharOffset reverse() {
        if (offset == 0) {
            return NONE;
        } else {
            return new SneakCharOffset(-offset);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + offset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SneakCharOffset other = (SneakCharOffset) obj;
        if (offset != other.offset)
            return false;
        return true;
    }

    public boolean equalsAmountOfChars(SneakCharOffset searchOffset) {
        return Math.abs(offset) == Math.abs(searchOffset.offset);
    }

    public int getOffset() {
        return offset;
    }
}
