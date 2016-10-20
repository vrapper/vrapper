package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class PercentMotion extends AbstractMotion {

    public static final Motion INSTANCE = new PercentMotion();

    @Override
    public Motion withCount(int count) {
        if (count > 100) {
            return DummyMotion.INSTANCE;
        } else if (count > 0) {
            return new PercentOfFileMotion(count);
        } else {
            return ParenthesesMove.INSTANCE;
        }
    }

    @Override
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        return ParenthesesMove.INSTANCE.destination(editorAdaptor);
    }

    @Override
    public BorderPolicy borderPolicy() {
        return ParenthesesMove.INSTANCE.borderPolicy();
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return ParenthesesMove.INSTANCE.stickyColumnPolicy();
    }

    @Override
    public boolean isJump() {
        return ParenthesesMove.INSTANCE.isJump();
    }

}
