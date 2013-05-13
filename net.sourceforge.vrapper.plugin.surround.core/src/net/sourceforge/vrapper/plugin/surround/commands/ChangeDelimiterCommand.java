package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.plugin.surround.mode.ReplaceDelimiterMode;
import net.sourceforge.vrapper.plugin.surround.state.AbstractDynamicDelimiterHolder;
import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

public class ChangeDelimiterCommand extends CountAwareCommand {

    private final DelimitedText delimitedText;
    private final DelimiterHolder replacement;

    public ChangeDelimiterCommand(DelimitedText delimitedText, DelimiterHolder replacement) {
        this.delimitedText = delimitedText;
        this.replacement = replacement;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (replacement instanceof AbstractDynamicDelimiterHolder) {
            // Update delimiters using this extension mode
            ReplaceDelimiterMode.switchMode(editorAdaptor, delimitedText,
                    (AbstractDynamicDelimiterHolder) replacement);
        } else {
            try {
                editorAdaptor.getHistory().beginCompoundChange();
                TextRange rightRange = delimitedText.rightDelimiter(editorAdaptor, count);
                TextRange leftRange = delimitedText.leftDelimiter(editorAdaptor, count);
                change(editorAdaptor, rightRange, replacement.getRight());
                change(editorAdaptor, leftRange, replacement.getLeft());
                // TODO: option to turn it off?
                editorAdaptor.getCursorService().setPosition(leftRange.getLeftBound(), true);
            } finally {
                editorAdaptor.getHistory().endCompoundChange();
            }
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
