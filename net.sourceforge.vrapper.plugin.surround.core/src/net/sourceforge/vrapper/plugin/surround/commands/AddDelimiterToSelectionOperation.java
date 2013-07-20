package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.plugin.surround.state.AbstractDynamicDelimiterHolder;
import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.InsertLineCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;

public class AddDelimiterToSelectionOperation implements TextOperation, DelimiterChangedListener {

    private DelimiterHolder replacement;
    private TextOperation indentOperation;

    public AddDelimiterToSelectionOperation(DelimiterHolder delimiters, TextOperation indentOperation) {
        this.indentOperation = indentOperation;

        //If the delimiter is dynamic, it must be updated through delimiterChanged(..)
        if ( ! (delimiters instanceof AbstractDynamicDelimiterHolder)) {
            this.replacement = delimiters;
        }
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count, TextObject selection)
            throws CommandExecutionException {
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock("surroundvisual");
            
            if (replacement == null) {
                throw new CommandExecutionException("Dynamic delimiter was never updated!");
            }
            if (selection.getContentType(editorAdaptor.getConfiguration())
                    .equals(ContentType.LINES)) {

                surroundLines(editorAdaptor, selection);

            } else if (selection.getContentType(editorAdaptor.getConfiguration())
                    .equals(ContentType.TEXT_RECTANGLE)) {

                surroundLinesInBlock(editorAdaptor, selection);

            } else {
                ChangeDelimiterCommand.doIt(editorAdaptor, 0, new NotYetDelimitedTextObject(selection),
                        replacement);
            }
        } finally {
            editorAdaptor.getHistory().unlock("surroundvisual");
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    private void surroundLines(EditorAdaptor editorAdaptor, TextObject selection)
            throws CommandExecutionException {
        TextContent content = editorAdaptor.getModelContent();
        TextRange range = selection.getRegion(editorAdaptor, 0);
        CursorService cursor = editorAdaptor.getCursorService();

        Position startContents = range.getLeftBound();
        Position endContents = range.getRightBound().addModelOffset(-1); //Exclusive bound

        //Use marks to keep correct Positions
        cursor.setMark(CursorService.LAST_CHANGE_START, startContents);
        cursor.setMark(CursorService.LAST_CHANGE_END, endContents);

        //Create new lines before and after selection
        LineInformation startLine = content.getLineInformationOfOffset(startContents.getModelOffset());
        InsertLineCommand.doIt(editorAdaptor, InsertLineCommand.Type.PRE_CURSOR, startLine);
        Position startSurround = cursor.getPosition(); // End of first inserted line

        startContents = cursor.getMark(CursorService.LAST_CHANGE_START);
        endContents = cursor.getMark(CursorService.LAST_CHANGE_END);

        LineInformation endLine = content.getLineInformationOfOffset(endContents.getModelOffset());
        InsertLineCommand.doIt(editorAdaptor, InsertLineCommand.Type.POST_CURSOR, endLine);
        Position endSurround = cursor.getPosition(); // End of last insterted line
        
        //Create new selection, offsets got shifted due to first inserted line.
        selection = new LineWiseSelection(editorAdaptor, startContents, endContents);

        //Save position in mark to keep it updated, indentation would otherwise invalidate them.
        cursor.setMark(CursorService.LAST_CHANGE_START, startSurround);
        cursor.setMark(CursorService.LAST_CHANGE_END, endSurround);
        
        if (indentOperation != null) {
            indentOperation.execute(editorAdaptor, Command.NO_COUNT_GIVEN, selection);
        }

        //Create new selection, offsets might have shifted due to indentation.
        selection = new SimpleSelection(
                new StartEndTextRange(cursor.getMark(CursorService.LAST_CHANGE_START),
                                      cursor.getMark(CursorService.LAST_CHANGE_END)));
        ChangeDelimiterCommand.doIt(editorAdaptor, 0, new NotYetDelimitedTextObject(selection),
                replacement);
    }

    private void surroundLinesInBlock(EditorAdaptor editorAdaptor,
            TextObject selection) throws CommandExecutionException {
        TextRange range = selection.getRegion(editorAdaptor, Command.NO_COUNT_GIVEN);
        TextBlock textBlock = BlockWiseSelection.getTextBlock(range.getStart(), range.getEnd(),
                editorAdaptor.getModelContent(), editorAdaptor.getCursorService());
        // TODO: Surround each block item
        new ChangeDelimiterCommand(new NotYetDelimitedTextObject(selection), replacement)
                .execute(editorAdaptor);
    }

    /**
     * Not supported (for now).
     */
    @Override
    public TextOperation repetition() {
        return null;
    }

    @Override
    public void delimiterChanged(DelimiterHolder from, DelimiterHolder to) {
        this.replacement = to;
    }
}
