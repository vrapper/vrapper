package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class CountedMotion implements Motion {

    private final int count;
    private final CountAwareMotion motion;

    public CountedMotion(int count, CountAwareMotion motion) {
        this.count = count;
        this.motion = motion;
    }

    public Position destination(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        return motion.destination(editorAdaptor, count);
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return motion.stickyColumnPolicy();
    }

    public BorderPolicy borderPolicy() {
        return motion.borderPolicy();
    }

    public int getCount() {
        return count;
    }

    public Motion withCount(int count) {
        return new CountedMotion(count, motion);
    }

    public boolean isJump() {
        return motion.isJump();
    }

    @Override
    public <T> T getAdapter(Class<T> type) {
        return motion.getAdapter(type);
    }
}
