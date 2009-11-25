package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;

public class SimplePositionlessSelection extends PositionlessSelection {

    private int trailingCharacters;

    public SimplePositionlessSelection(EditorAdaptor editorAdaptor, SimpleSelection selection) {
        TextContent modelContent = editorAdaptor.getModelContent();
         LineInformation startLine = modelContent.getLineInformationOfOffset(selection.getLeftBound().getModelOffset());
        LineInformation endLine = modelContent.getLineInformationOfOffset(selection.getRightBound().getModelOffset());
        linesSpanned = endLine.getNumber() - startLine.getNumber() + 1;
        if (linesSpanned == 1)
        	trailingCharacters = selection.getModelLength();
        else
        	trailingCharacters = selection.getRightBound().getModelOffset() - endLine.getBeginOffset();
    }

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) {
        TextContent modelContent = editorAdaptor.getModelContent();
        Position start = editorAdaptor.getPosition();
        Position end;
        if (linesSpanned == 1) {
            end = start.addModelOffset(trailingCharacters);
        } else {
            LineInformation firstLine = modelContent.getLineInformationOfOffset(start.getModelOffset());
            LineInformation lastLine = modelContent.getLineInformation(firstLine.getNumber() + linesSpanned - 1);
            end = editorAdaptor.getCursorService().newPositionForModelOffset(lastLine.getBeginOffset()).addModelOffset(trailingCharacters);
        }
        return new StartEndTextRange(start, end);
    }

	public ContentType getContentType(Configuration configuration) {
		return ContentType.TEXT;
	}


}
