package net.sourceforge.vrapper.vim.modes;

public class WithCountHint implements ModeSwitchHint {
    private final int count;

    public WithCountHint(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}