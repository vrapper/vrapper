package net.sourceforge.vrapper.vim.commands.motions;

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
