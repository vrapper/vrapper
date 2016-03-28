package net.sourceforge.vrapper.vim.commands;

public abstract class AbstractTextObject implements TextObject {
    public TextObject withCount(int count) {
        return new MultipliedTextObject(count, this);
    }

    public int getCount() {
        return 1;
    }

    @Override
    public TextObject repetition() {
        return this;
    }
}
