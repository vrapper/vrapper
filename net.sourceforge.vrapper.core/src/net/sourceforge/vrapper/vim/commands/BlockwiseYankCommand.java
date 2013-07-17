package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class BlockwiseYankCommand extends AbstractCommand {

    public final static BlockwiseYankCommand INSTANCE = new BlockwiseYankCommand();

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        final TextObject selection = editorAdaptor.getSelection();
        final TextRange blockRange = selection.getRegion(editorAdaptor, NO_COUNT_GIVEN);
        YankOperation.doIt(editorAdaptor, blockRange, ContentType.TEXT_RECTANGLE, true);
        LeaveVisualModeCommand.doIt(editorAdaptor);
    }

    @Override
    public Command repetition() {
        return this;
    }

    @Override
    public Command withCount(int count) {
        return this;
    }

}
