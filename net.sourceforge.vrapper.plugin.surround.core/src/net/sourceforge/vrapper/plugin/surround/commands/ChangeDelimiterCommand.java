package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

public class ChangeDelimiterCommand extends CountAwareCommand {

    private final DelimitedText delimitedText;
    private final DelimiterHolder delimiters;

    public ChangeDelimiterCommand(DelimitedText delimitedText, DelimiterHolder delimiters) {
        this.delimitedText = delimitedText;
        this.delimiters = delimiters;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            TextRange rightRange = delimitedText.rightDelimiter(editorAdaptor, count);
            TextRange leftRange = delimitedText.leftDelimiter(editorAdaptor, count);
            change(editorAdaptor, rightRange, delimiters.getRight());
            change(editorAdaptor, leftRange, delimiters.getLeft());
            // TODO: option to turn it off?
            editorAdaptor.getCursorService().setPosition(leftRange.getLeftBound(), true);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }
    
    private void change(EditorAdaptor editorAdaptor, TextRange range, String to) {
        TextContent modelContent = editorAdaptor.getModelContent();
        modelContent.replace(range.getLeftBound().getModelOffset(), range.getModelLength(), to);
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }

}
