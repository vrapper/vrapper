package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class JoinVisualLinesCommand extends AbstractCommand {
    public static final Command INSTANCE = new JoinVisualLinesCommand(true);
    public static final Command DUMB_INSTANCE = new JoinVisualLinesCommand(false);
    private final boolean isSmart;

    private JoinVisualLinesCommand(boolean isSmart) {
        this.isSmart = isSmart;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        Selection selection = editorAdaptor.getSelection();
        Position from = selection.getLeftBound();
        Position to = selection.getRightBound();
        TextContent modelContent = editorAdaptor.getModelContent();
        int firstLineNo = modelContent.getLineInformationOfOffset(from.getModelOffset()).getNumber();
        int lastLineNo =  modelContent.getLineInformationOfOffset(to.getModelOffset()).getNumber();
        
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.changeMode(NormalMode.NAME);
            editorAdaptor.setPosition(from, false);
            JoinLinesCommand.doIt(editorAdaptor, lastLineNo - firstLineNo, isSmart);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public Command repetition() {
        return this;
    }

    public Command withCount(int count) {
        return this;
    }

}
