package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class BlockWiseSelectionArea extends SelectionArea {

    private final int colsSpanned;

    public BlockWiseSelectionArea(final EditorAdaptor editorAdaptor,
            final BlockWiseSelection selection) {
        final Rect rect = BlockWiseSelection.getRect(editorAdaptor.getModelContent(), selection);
        linesSpanned = rect.height();
        colsSpanned = rect.width();
    }

    @Override
    public TextRange getRegion(final EditorAdaptor editorAdaptor, final int count)
            throws CommandExecutionException {

        final TextContent modelContent = editorAdaptor.getModelContent();
        final Position start = editorAdaptor.getPosition();
        Position end;
        if (linesSpanned == 1) {
            end = start.addModelOffset(colsSpanned);
        } else {
            final LineInformation firstLine = modelContent.getLineInformationOfOffset(start.getModelOffset());
            final LineInformation lastLine = modelContent.getLineInformation(firstLine.getNumber() + linesSpanned - 1);
            end = editorAdaptor.getCursorService().newPositionForModelOffset(lastLine.getBeginOffset())
                    .addModelOffset(VimUtils.calculateColForPosition(modelContent, start))
                    .addModelOffset(colsSpanned);
        }
        return new StartEndTextRange(start, end);
    
    }

    @Override
    public ContentType getContentType(final Configuration configuration) {
        return ContentType.TEXT_RECTANGLE;
    }

}
