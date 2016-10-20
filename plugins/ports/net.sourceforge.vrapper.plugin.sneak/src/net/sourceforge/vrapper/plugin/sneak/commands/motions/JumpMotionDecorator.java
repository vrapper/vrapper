package net.sourceforge.vrapper.plugin.sneak.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.AbstractMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Makes a Motion declare itself as one which jumps.
 */
public class JumpMotionDecorator extends AbstractMotion implements Motion {
    protected Motion delegate;

    public JumpMotionDecorator(Motion motion) {
        delegate = motion;
    }

    public Motion withCount(int count) {
        return new JumpMotionDecorator(delegate.withCount(count));
    }

    public int getCount() {
        return delegate.getCount();
    }

    public Position destination(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        return delegate.destination(editorAdaptor);
    }

    public BorderPolicy borderPolicy() {
        return delegate.borderPolicy();
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return delegate.stickyColumnPolicy();
    }

    public boolean isJump() {
        return true;
    }

    @Override
    public <T> T getAdapter(Class<T> type) {
        return delegate.getAdapter(type);
    }
}
