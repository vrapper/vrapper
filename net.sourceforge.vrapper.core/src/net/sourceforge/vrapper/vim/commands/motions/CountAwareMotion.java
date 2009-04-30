package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public abstract class CountAwareMotion implements Motion {

    public abstract Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException;

    public Position destination(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        return destination(editorAdaptor, 1);
    }

    public int getCount() {
        return 1;
    }

    public Motion withCount(int count) {
        return new CountedMotion(count, this);
    }

}
