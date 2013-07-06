package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class BlockWiseSelectionArea extends SelectionArea {

    private final int visualWidth;

    public BlockWiseSelectionArea(final EditorAdaptor editorAdaptor,
            final BlockWiseSelection selection) {
        final TextBlock rect = BlockWiseSelection.getTextBlock(selection.getFrom(), selection.getTo(), editorAdaptor.getModelContent(), editorAdaptor.getCursorService());
        visualWidth = rect.endVisualOffset - rect.startVisualOffset;
        linesSpanned = rect.endLine - rect.startLine + 1;
    }

    @Override
    public TextRange getRegion(final EditorAdaptor editorAdaptor, final int count)
            throws CommandExecutionException {

        final TextContent modelContent = editorAdaptor.getModelContent();
        final Position start = editorAdaptor.getPosition();
        final LineInformation firstLine = modelContent.getLineInformationOfOffset(start.getModelOffset());
        final LineInformation lastLine = modelContent.getLineInformation(firstLine.getNumber() + linesSpanned - 1);
        final CursorService cursorService = editorAdaptor.getCursorService();
        final int startVOffset = cursorService.getVisualOffset(start);
        Position end = cursorService.getPositionByVisualOffset(lastLine.getNumber(), startVOffset + visualWidth);
        if (end == null) {
            // TODO : applying a visual block to a line with no content at the block rect.
            end = cursorService.newPositionForModelOffset(lastLine.getEndOffset());
        }
        return new StartEndTextRange(start, end);
    
    }

    @Override
    public ContentType getContentType(final Configuration configuration) {
        return ContentType.TEXT_RECTANGLE;
    }

}
