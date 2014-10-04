package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public abstract class AbstractVisualMotionCommand extends MotionCommand {

    public AbstractVisualMotionCommand(Motion motion) {
        super(motion);
    }

    protected abstract void extendSelection(EditorAdaptor editorAdaptor, Selection oldSelection,
            int motionCount);

    protected abstract Selection getSelection(EditorAdaptor editorAdaptor);

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        Object redrawLock = new Object();
        ViewportService viewportService = editorAdaptor.getViewportService();
        try {
            viewportService.setRepaint(false);
            viewportService.lockRepaint(redrawLock);

            Selection oldSelection = getSelection(editorAdaptor);
            editorAdaptor.setPosition(oldSelection.getTo(), StickyColumnPolicy.NEVER);
            super.execute(editorAdaptor, count);
            extendSelection(editorAdaptor, oldSelection, count);

        } finally {
            viewportService.unlockRepaint(redrawLock);
            viewportService.setRepaint(true);
        }
    }

}