package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;

public class LineWiseSelectionArea extends SelectionArea {

    public LineWiseSelectionArea(EditorAdaptor editorAdaptor, LineWiseSelection selection) {
        TextContent modelContent = editorAdaptor.getModelContent();
        int startLine = modelContent.getLineInformationOfOffset(selection.getLeftBound().getModelOffset()).getNumber();
        int endLine = modelContent.getLineInformationOfOffset(selection.getRightBound().getModelOffset()).getNumber();
        this.linesSpanned = endLine - startLine;
    }

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) {
        TextContent modelContent = editorAdaptor.getModelContent();
        LineInformation firstLine = modelContent.getLineInformationOfOffset(editorAdaptor.getPosition().getModelOffset());
        LineInformation lastLine = modelContent.getLineInformation(firstLine.getNumber() + linesSpanned);
        Position start = editorAdaptor.getCursorService().newPositionForModelOffset(firstLine.getBeginOffset());
        Position end = editorAdaptor.getCursorService().newPositionForModelOffset(lastLine.getBeginOffset());
        return new StartEndTextRange(start, end);
    }

	public ContentType getContentType(Configuration configuration) {
		return ContentType.LINES;
	}


}
