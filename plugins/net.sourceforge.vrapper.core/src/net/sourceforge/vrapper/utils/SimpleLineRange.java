package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class SimpleLineRange implements LineRange {

    /**
     * Returns a {@link LineRange} for the entire file.
     * <p><b>NOTE</b>: the very last line is not used as endLine if it is empty, this to implement a
     * behavior similar to Vim where the global command does not put text after the last newline.
     */
    public static SimpleLineRange entireFile(EditorAdaptor editorAdaptor) {
        SimpleLineRange result = new SimpleLineRange();
        TextContent mc = editorAdaptor.getModelContent();
        CursorService cs = editorAdaptor.getCursorService();
        result.startLine = 0;
        result.endLine = mc.getNumberOfLines() - 1;
        result.modelLength = mc.getTextLength();
        LineInformation lastLine = mc.getLineInformationOfOffset(mc.getTextLength());
        if (lastLine.getLength() == 0 && result.endLine > 0) {
            result.endLine--;
        }
        result.from = cs.newPositionForModelOffset(0);
        result.to = cs.shiftPositionForModelOffset(mc.getTextLength(), -1, false);
        return result;
    }

    /** Returns a line range for a single line. To and From are set based on current cursor pos. */
    public static SimpleLineRange singleLine(EditorAdaptor editorAdaptor, Position position) {
        int modelOffset = position.getModelOffset();
        TextContent mc = editorAdaptor.getModelContent();
        SimpleLineRange result = singleLineInModel(editorAdaptor,
                mc.getLineInformationOfOffset(modelOffset).getNumber());
        result.from = result.to = position;
        return result;
    }

    /** Returns a line range for a single line. To and From are set at the start of the line. */
    public static SimpleLineRange singleLineInModel(EditorAdaptor editorAdaptor,
            LineInformation modelLine) {
        SimpleLineRange result = new SimpleLineRange();
        result.startLine = result.endLine = modelLine.getNumber();
        CursorService cs = editorAdaptor.getCursorService();
        // Shift past line end into next line. When at EOF, we get back what we started with.
        Position nextLineStart = cs.shiftPositionForModelOffset(modelLine.getEndOffset(), 1, false);
        //if nextLineStart goes beyond EOF, it's coming back as less than modelLine.getEndOffset.  Compensate for that scenario
        result.modelLength = Math.max(nextLineStart.getModelOffset(), modelLine.getEndOffset()) - modelLine.getBeginOffset();
        result.from = result.to = cs.newPositionForModelOffset(modelLine.getBeginOffset());
        return result;
    }

    /** Returns a line range for a single line. To and From are set at the start of the line. */
    public static SimpleLineRange singleLineInModel(EditorAdaptor editorAdaptor, int modelLine) {
        TextContent mc = editorAdaptor.getModelContent();
        LineInformation lineInfo = mc.getLineInformation(modelLine);
        return singleLineInModel(editorAdaptor, lineInfo);
    }

    /** Returns a line range where both positions are included. */
    public static SimpleLineRange betweenPositions(EditorAdaptor editorAdaptor, Position from, Position to) {
        SimpleLineRange result = new SimpleLineRange();
        LineInformation startLine;
        LineInformation endLine;
        TextContent mc = editorAdaptor.getModelContent();
        if (to.compareTo(from) < 0) {
            startLine = mc.getLineInformationOfOffset(to.getModelOffset());
            endLine = mc.getLineInformationOfOffset(from.getModelOffset());
        } else {
            startLine = mc.getLineInformationOfOffset(from.getModelOffset());
            endLine = mc.getLineInformationOfOffset(to.getModelOffset());
        }
        result.startLine = startLine.getNumber();
        result.endLine = endLine.getNumber();
        result.modelLength = StartEndTextRange.lines(editorAdaptor, from, to).getModelLength();
        result.from = from;
        result.to = to;
        return result;
    }

    /** Calculates a line range based on the from and to position of the selection. */
    public static SimpleLineRange fromSelection(EditorAdaptor editorAdaptor, Selection selection) {
        SimpleLineRange result = new SimpleLineRange();
        LineInformation startLine;
        LineInformation endLine;
        TextContent mc = editorAdaptor.getModelContent();
        Position from = selection.getFrom();
        Position to = selection.getTo();
        if (selection.isReversed()) {
            startLine = mc.getLineInformationOfOffset(to.getModelOffset());
            endLine = mc.getLineInformationOfOffset(from.getModelOffset());
        } else {
            startLine = mc.getLineInformationOfOffset(from.getModelOffset());
            endLine = mc.getLineInformationOfOffset(to.getModelOffset());
        }
        result.startLine = startLine.getNumber();
        result.endLine = endLine.getNumber();
        if (selection instanceof LineWiseSelection) {
            result.modelLength = selection.getModelLength();
        } else {
            result.modelLength = StartEndTextRange.lines(editorAdaptor, from, to).getModelLength();
        }
        result.from = from;
        result.to = to;
        return result;
    }

    /**
     * Returns a {@link LineRange} wrapping the given text region.
     * <p><b>NOTE</b>: the very last line (the one rightBound is in) is not used as endLine if no
     * characters are selected or if it is empty.
     */
    public static SimpleLineRange fromTextRange(EditorAdaptor editorAdaptor, TextRange textRange) {
        Position start = textRange.getLeftBound();
        Position end = textRange.getRightBound();
        int endOffset = end.getModelOffset();
        TextContent mc = editorAdaptor.getModelContent();
        LineInformation lastLine = mc.getLineInformationOfOffset(endOffset);
        CursorService cs = editorAdaptor.getCursorService();
        // Check that the right bound includes some characters, if not we move to previous line
        if (endOffset == lastLine.getBeginOffset()) {
            end = cs.shiftPositionForModelOffset(endOffset, -1, false);
        }
        return betweenPositions(editorAdaptor, start, end);
    }

    protected int startLine;
    protected int endLine;
    protected int modelLength;
    private Position from;
    private Position to;

    protected SimpleLineRange() {
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public int getModelLength() {
        return modelLength;
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        LineInformation startInfo = editorAdaptor.getModelContent().getLineInformation(startLine);
        int startOffset = startInfo.getBeginOffset();
        Position start = editorAdaptor.getCursorService().newPositionForModelOffset(startOffset);
        return StartEndTextRange.exclusive(start, start.addModelOffset(modelLength));
    }

    @Override
    public ContentType getContentType(Configuration configuration) {
        return ContentType.LINES;
    }

    @Override
    public TextObject withCount(int count) {
        return this;
    }

    @Override
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public TextObject repetition() {
        return this;
    }

    @Override
    public Position getFrom() {
        return from;
    }

    @Override
    public Position getTo() {
        return to;
    }
}
