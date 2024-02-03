package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Motion which literally does nothing - no move, no change in a text (selection) range.
 */
public class DummyMotion extends AbstractModelSideMotion {

    public static final Motion INSTANCE = new DummyMotion();

    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    protected int destination(int offset, TextContent content, int count)
            throws CommandExecutionException {
        return offset;
    }
}
