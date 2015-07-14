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

    public static SimpleLineRange singleLine(EditorAdaptor editorAdaptor, Position position) {
        int modelOffset = position.getModelOffset();
        TextContent mc = editorAdaptor.getModelContent();
        SimpleLineRange result = singleLineInModel(editorAdaptor,
                mc.getLineInformationOfOffset(modelOffset).getNumber());
        result.from = result.to = position;
        return result;
    }

    public static SimpleLineRange singleLineInModel(EditorAdaptor editorAdaptor,
            LineInformation modelLine) {
        SimpleLineRange result = new SimpleLineRange();
        result.startLine = result.endLine = modelLine.getNumber();
        CursorService cs = editorAdaptor.getCursorService();
        // Shift past line end into next line. When at EOF, we get back what we started with.
        Position nextLineStart = cs.shiftPositionForModelOffset(modelLine.getEndOffset(), 1, false);
        result.modelLength = nextLineStart.getModelOffset() - modelLine.getBeginOffset();
        result.from = result.to = cs.newPositionForModelOffset(modelLine.getBeginOffset());
        return result;
    }

    public static SimpleLineRange singleLineInModel(EditorAdaptor editorAdaptor, int modelLine) {
        TextContent mc = editorAdaptor.getModelContent();
        LineInformation lineInfo = mc.getLineInformation(modelLine);
        return singleLineInModel(editorAdaptor, lineInfo);
    }

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
    public Position getFrom() {
        return from;
    }

    @Override
    public Position getTo() {
        return to;
    }
}
