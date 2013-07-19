package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;

public class AddDelimiterToSelectionOperation implements TextOperation {

    private DelimiterHolder delimiters;
    private TextOperation indentOperation;

    public AddDelimiterToSelectionOperation(DelimiterHolder delimiters, TextOperation indentOperation) {
        this.indentOperation = indentOperation;
        this.delimiters = delimiters;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count, TextObject selection)
            throws CommandExecutionException {
        if (selection.getContentType(editorAdaptor.getConfiguration())
                .equals(ContentType.LINES)) {
            surroundLines(editorAdaptor, selection);

        } else if (selection.getContentType(editorAdaptor.getConfiguration())
                .equals(ContentType.TEXT_RECTANGLE)) {
            surroundLinesInBlock(editorAdaptor, selection);

        } else {
            new ChangeDelimiterCommand(new NotYetDelimitedTextObject(selection), delimiters)
                    .execute(editorAdaptor);
        }
    }

    private void surroundLines(EditorAdaptor editorAdaptor, TextObject selection)
            throws CommandExecutionException {
        if (indentOperation != null) {
            indentOperation.execute(editorAdaptor, Command.NO_COUNT_GIVEN, selection);
        }
        // TODO: Put delimiters on line before and after line-wise selection
        new ChangeDelimiterCommand(new NotYetDelimitedTextObject(selection), delimiters)
                .execute(editorAdaptor);
    }

    private void surroundLinesInBlock(EditorAdaptor editorAdaptor,
            TextObject selection) throws CommandExecutionException {
        TextRange range = selection.getRegion(editorAdaptor, Command.NO_COUNT_GIVEN);
        TextBlock textBlock = BlockWiseSelection.getTextBlock(range.getStart(), range.getEnd(),
                editorAdaptor.getModelContent(), editorAdaptor.getCursorService());
        // TODO: Surround each block item
        new ChangeDelimiterCommand(new NotYetDelimitedTextObject(selection), delimiters)
                .execute(editorAdaptor);
    }

    /**
     * Not supported (for now), use <tt>gv</tt> to restore the selection and re-enter this command.
     */
    @Override
    public TextOperation repetition() {
        return null;
    }
}
