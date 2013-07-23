package net.sourceforge.vrapper.plugin.surround.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.plugin.surround.mode.ReplaceDelimiterMode;
import net.sourceforge.vrapper.plugin.surround.state.AbstractDynamicDelimiterHolder;
import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.InsertLineCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;

public class AddDelimiterToSelectionCommand implements Command, DelimiterChangedListener {

    private AbstractDynamicDelimiterHolder dynamicDelimiter;
    private DelimiterHolder replacement;
    /**
     * Indent operation to be applied on the selection. Vrapper core has no such implementation,
     * pass it in from a higher level.
     */
    private TextOperation indentOperation;
    private boolean isRepetition;
    /**
     * Whether this command was invoked as 'vS' or as 'vgS'. The latter does no indentation in
     * linewise mode, and it surrounds blocks with extra padding if the block width
     * is not consistent.
     */
    private boolean isGMode;

    public AddDelimiterToSelectionCommand(DelimiterHolder delimiters, boolean isGMode,
            TextOperation indentOperation) {
        this.indentOperation = indentOperation;
        this.isGMode = isGMode;

        //If the delimiter is dynamic, it must be updated through delimiterChanged(..)
        if (delimiters instanceof AbstractDynamicDelimiterHolder) {
            this.dynamicDelimiter = (AbstractDynamicDelimiterHolder) delimiters;
        } else {
            this.replacement = delimiters;
        }
    }

    protected AddDelimiterToSelectionCommand(AddDelimiterToSelectionCommand original) {
        this.dynamicDelimiter = original.dynamicDelimiter;
        this.replacement = original.replacement;
        this.isGMode = original.isGMode;
        this.indentOperation = original.indentOperation;
        this.isRepetition = true;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        TextObject selection;
        if (isRepetition) {
            selection = editorAdaptor.getLastActiveSelectionArea();
        } else {
            editorAdaptor.rememberLastActiveSelection();
            selection = editorAdaptor.getSelection();
        }
        addDelimiters(editorAdaptor, selection);
        if (dynamicDelimiter == null) {
            LeaveVisualModeCommand.doIt(editorAdaptor);
        }
    }

    protected void addDelimiters(EditorAdaptor editorAdaptor, TextObject selection)
            throws CommandExecutionException {
        if (dynamicDelimiter != null && replacement == null) {
            ReplaceDelimiterMode.switchMode(editorAdaptor, this, this,
                    new NotYetDelimitedTextObject(selection), dynamicDelimiter);
        } else {
            try {
                editorAdaptor.getHistory().beginCompoundChange();
                editorAdaptor.getHistory().lock("surroundvisual");

                if (selection.getContentType(editorAdaptor.getConfiguration())
                        .equals(ContentType.LINES)) {

                    surroundLines(editorAdaptor, selection);

                } else if (selection.getContentType(editorAdaptor.getConfiguration())
                        .equals(ContentType.TEXT_RECTANGLE)) {

                    surroundLinesInBlock(editorAdaptor, selection);

                } else {
                    ChangeDelimiterCommand.doIt(editorAdaptor, 0,
                            new NotYetDelimitedTextObject(selection), replacement);
                }
            } finally {
                editorAdaptor.getHistory().unlock("surroundvisual");
                editorAdaptor.getHistory().endCompoundChange();
            }
        }
    }

    protected void surroundLines(EditorAdaptor editorAdaptor, TextObject selection)
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
        
        if ( ! isGMode && indentOperation != null) {
            indentOperation.execute(editorAdaptor, Command.NO_COUNT_GIVEN, selection);
        }

        //Create new selection, offsets might have shifted due to indentation.
        selection = new SimpleSelection(
                new StartEndTextRange(cursor.getMark(CursorService.LAST_CHANGE_START),
                                      cursor.getMark(CursorService.LAST_CHANGE_END)));
        ChangeDelimiterCommand.doIt(editorAdaptor, 0, new NotYetDelimitedTextObject(selection),
                replacement);
    }

    protected void surroundLinesInBlock(EditorAdaptor editorAdaptor, TextObject selection)
            throws CommandExecutionException {
        CursorService cursor = editorAdaptor.getCursorService();
        TextContent content = editorAdaptor.getModelContent();
        TextRange range = selection.getRegion(editorAdaptor, Command.NO_COUNT_GIVEN);
        TextBlock textBlock = BlockWiseSelection.getTextBlock(range.getStart(), range.getEnd(),
                content, cursor);

        for (int line = textBlock.startLine; line <= textBlock.endLine; ++line) {
            final LineInformation lineInfo = content.getLineInformation(line);
            Position start = cursor.getPositionByVisualOffset(line, textBlock.startVisualOffset);
            if (start == null) {
                //No characters at the start visual offset, fill with spaces
                start = fillWithSpacesUntil(line, textBlock.startVisualOffset, cursor, content);
            }
            Position end = cursor.getPositionByVisualOffset(line, textBlock.endVisualOffset);
            if (end == null) {
                final boolean pastEOL = start.getModelOffset() > lineInfo.getEndOffset();
                //No characters at the end visual offset, fill with spaces
                if (isGMode || pastEOL) {
                    end = fillWithSpacesUntil(line, textBlock.endVisualOffset, cursor, content);
                    //Insert space so the delimiter is inserted _after_ the block.
                    content.replace(end.getModelOffset(), 0, " ");
                } else {
                    end = cursor.newPositionForModelOffset(lineInfo.getEndOffset() - 1);
                }
            }
            content.replace(start.getModelOffset(), 0, replacement.getLeft());
            content.replace(end.addModelOffset(1 + replacement.getLeft().length()).getModelOffset(),
                    0, replacement.getRight());
        }
        cursor.setPosition(
                cursor.getPositionByVisualOffset(textBlock.startLine, textBlock.startVisualOffset),
                true);
    }

    protected Position fillWithSpacesUntil(int line, int visualOffset,
            final CursorService cursorService, final TextContent content) {
        LineInformation lineInfo = content.getLineInformation(line);
        Position lineEnd = cursorService.newPositionForModelOffset(lineInfo.getEndOffset());
        final int lineEndVOfs = cursorService.getVisualOffset(lineEnd);
        final int padding = cursorService.visualWidthToChars(visualOffset - lineEndVOfs);
        content.replace(lineInfo.getEndOffset(), 0, StringUtils.multiply(" ", padding));
        return lineEnd.addModelOffset(padding);
    }

    @Override
    public Command repetition() {
        return new AddDelimiterToSelectionCommand(this);
    }

    @Override
    public Command withCount(int count) {
        return this;
    }

    @Override
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public void delimiterChanged(DelimiterHolder from, DelimiterHolder to) {
        this.replacement = to;
    }
}
