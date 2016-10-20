package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public abstract class AbstractMotion implements Motion {
    @Override
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public <T> T getAdapter(Class<T> type) {
        return null;
    }
}
