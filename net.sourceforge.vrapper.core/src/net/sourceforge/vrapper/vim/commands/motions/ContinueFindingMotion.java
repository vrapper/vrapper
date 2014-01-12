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
        Position dest = findMotion.destination(editorAdaptor, count);
        //If using 't' and the cursor is before the last match, destination()
        //will think this position is the next match and not move the cursor.
        //If this happens, move the cursor forward one (so it's on top of the
        //last match) and run destination() again.  If 'T', go back one.
        if(!findMotion.upToTarget && editorAdaptor.getPosition().getModelOffset() == dest.getModelOffset()) {
            int tweakOffset = findMotion.backwards ? -1 : 1;
            try {
                //move cursor to be on top of the last match
                editorAdaptor.setPosition(dest.addModelOffset(tweakOffset), StickyColumnPolicy.NEVER);
                //try again
                dest = findMotion.destination(editorAdaptor, count);
            }
            catch(CommandExecutionException e) {
                //no match, un-tweak the cursor position
                editorAdaptor.setPosition(dest.addModelOffset(tweakOffset * -1), StickyColumnPolicy.NEVER);
            }
        }
        return dest;
    }

    public BorderPolicy borderPolicy() {
        return borderPolicy;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

}
