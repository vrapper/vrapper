package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Motion responsible for repeating <code>t</code>, <code>T</code>, <code>f</code> and
 * <code>F</code> motions. Finds next occurrence of a character in current line.
 */
public class ContinueFindingMotion extends CountAwareMotion {

    public static final ContinueFindingMotion NORMAL = new ContinueFindingMotion(false, false);
    public static final ContinueFindingMotion REVERSE = new ContinueFindingMotion(true, false);

    public static final ContinueFindingMotion NORMAL_NAVIGATING = new ContinueFindingMotion(false, true);
    public static final ContinueFindingMotion REVERSE_NAVIGATING = new ContinueFindingMotion(true, true);

    private final boolean reverse;
    private final boolean useLastNavigatingMotion;
    // XXX: this is evil, motions shouldn't keep state. This depends on destination(..) always being
    // called before borderPolicy()
    private BorderPolicy borderPolicy = BorderPolicy.INCLUSIVE;

    private ContinueFindingMotion(boolean reverse, boolean useLastNavigatingMotion) {
        this.reverse = reverse;
        this.useLastNavigatingMotion = useLastNavigatingMotion;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition)
            throws CommandExecutionException {
        NavigatingMotion navigatingMotion = getLastNavigatingMotion(editorAdaptor);
        if (navigatingMotion == null) {
            throw new CommandExecutionException("no find to repeat");
        }
        borderPolicy = navigatingMotion.borderPolicy();
        Position dest = navigatingMotion.withCount(count).destination(editorAdaptor, fromPosition);
        return dest;
    }

    protected NavigatingMotion getLastNavigatingMotion(EditorAdaptor editorAdaptor) {
        NavigatingMotion navigatingMotion;
        if (useLastNavigatingMotion) {
            navigatingMotion = editorAdaptor.getRegisterManager().getLastNavigatingMotion();
        } else {
            navigatingMotion = editorAdaptor.getRegisterManager().getLastFindCharMotion();
        }
        if (navigatingMotion != null && reverse) {
            navigatingMotion = navigatingMotion.reverse();
        }
        return navigatingMotion;
    }

    public BorderPolicy borderPolicy() {
        return borderPolicy;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }
}
