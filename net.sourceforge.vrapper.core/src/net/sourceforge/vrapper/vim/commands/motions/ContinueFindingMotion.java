package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class ContinueFindingMotion extends CountAwareMotion {

    public static final ContinueFindingMotion NORMAL = new ContinueFindingMotion(false);
    public static final ContinueFindingMotion REVERSE = new ContinueFindingMotion(true);

    private final boolean reverse;
    // XXX: this is so evil
    private BorderPolicy borderPolicy = BorderPolicy.INCLUSIVE;

    private ContinueFindingMotion(boolean reverse) {
        this.reverse = reverse;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        FindMotion findMotion = editorAdaptor.getRegisterManager().getLastFindMotion();
        if (findMotion == null) {
            throw new CommandExecutionException("no find to repeat");
        }
        if (reverse) {
            findMotion = findMotion.reversed();
        }
        borderPolicy = findMotion.borderPolicy();
        return findMotion.destination(editorAdaptor, count);
    }

    public BorderPolicy borderPolicy() {
        return borderPolicy;
    }

    public boolean updateStickyColumn() {
        return true;
    }

}
