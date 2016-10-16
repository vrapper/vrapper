package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public abstract class CountAwareMotion extends AbstractMotion {

    public abstract Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;

    public Position destination(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        return destination(editorAdaptor, getCount());
    }

    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    public Motion withCount(int count) {
        return new CountedMotion(count, this);
    }

    public boolean isJump() {
        return false;
    }

}
