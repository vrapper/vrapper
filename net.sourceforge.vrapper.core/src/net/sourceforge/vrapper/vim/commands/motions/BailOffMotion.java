package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class BailOffMotion extends AbstractModelSideMotion {

    private final char delim;
    private final AbstractModelSideMotion wrapped;

    public BailOffMotion(char delim, AbstractModelSideMotion wrapped) {
        this.delim = delim;
        this.wrapped = wrapped;
    }

    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
        if (content.getText(offset, 1).charAt(0) == delim)
            if (count == 1)
                return offset;
            else
                return wrapped.destination(offset, content, count - 1);
        return wrapped.destination(offset, content, count);
    }

    public BorderPolicy borderPolicy() {
        return wrapped.borderPolicy();
    }

}
