package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class PerformOperationOnSearchResultCommand extends CountAwareCommand {
    private TextOperation operation;
    private Motion motion;
    
    public PerformOperationOnSearchResultCommand(TextOperation operation, Motion motion) {
        this.operation = operation;
        this.motion = motion;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        MotionTextObject mto = new MotionTextObject(motion.withCount(count));
        operation.execute(editorAdaptor, count, mto);
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }

}
