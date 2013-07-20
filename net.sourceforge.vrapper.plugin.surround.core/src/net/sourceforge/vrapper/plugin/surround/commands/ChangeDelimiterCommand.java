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

public class ChangeDelimiterCommand extends CountAwareCommand implements DelimiterChangedListener {

    private final DelimitedText delimitedText;
    private AbstractDynamicDelimiterHolder dynamicDelimiter;
    private DelimiterHolder replacement;

    public ChangeDelimiterCommand(DelimitedText delimitedText, DelimiterHolder replacement) {
        this.delimitedText = delimitedText;
        if (replacement instanceof AbstractDynamicDelimiterHolder) {
            this.dynamicDelimiter = (AbstractDynamicDelimiterHolder) replacement;
        } else {
            this.replacement = replacement;
        }
    }

    @Override
    public void delimiterChanged(DelimiterHolder from, DelimiterHolder to) {
        replacement = to;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (dynamicDelimiter != null && replacement == null) {
            //Update replacement delimiter first, which will be injected using delimiterChanged(..).
            ReplaceDelimiterMode.switchMode(editorAdaptor, this, this, delimitedText, dynamicDelimiter);
        } else {
            try {
                editorAdaptor.getHistory().beginCompoundChange();
                doIt(editorAdaptor, count, delimitedText, replacement);
            } finally {
                editorAdaptor.getHistory().endCompoundChange();
            }
        }
    }

    public static void doIt(EditorAdaptor editorAdaptor, int count, DelimitedText delimitedText,
            DelimiterHolder replacement)
            throws CommandExecutionException {
        TextRange rightRange = delimitedText.rightDelimiter(editorAdaptor, count);
        TextRange leftRange = delimitedText.leftDelimiter(editorAdaptor, count);
        change(editorAdaptor, rightRange, replacement.getRight());
        change(editorAdaptor, leftRange, replacement.getLeft());
        // TODO: option to turn it off?
        editorAdaptor.getCursorService().setPosition(leftRange.getLeftBound(), true);
    }

    private static void change(EditorAdaptor editorAdaptor, TextRange range, String to) {
        TextContent modelContent = editorAdaptor.getModelContent();
        modelContent.replace(range.getLeftBound().getModelOffset(), range.getModelLength(), to);
    }

    @Override
    public CountAwareCommand repetition() {
        if (dynamicDelimiter != null) {
            return new ChangeDelimiterCommand(delimitedText, dynamicDelimiter);
        } else {
            return this;
        }
    }

}
