package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;

public class MoveDownReturn extends CountAwareMotion {

    public static final MoveDownReturn INSTANCE = new MoveDownReturn();

    private MoveDownReturn() { /* NOP */ }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        MotionCommand.doIt(editorAdaptor, MoveDown.INSTANCE.withCount(count));
        return LineStartMotion.NON_WHITESPACE.destination(editorAdaptor);
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    public boolean updateStickyColumn() {
        return true;
    }

}
