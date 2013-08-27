package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.TextBlockRegisterContent;

/**
 * Universal block paste implementation.
 */
abstract public class BlockPasteHelper {

    static public void execute(EditorAdaptor editorAdaptor, int count, int startOfs, boolean placeCursorAfter) {
        final CursorService cursorService = editorAdaptor.getCursorService();
        RegisterContent registerContent = editorAdaptor.getRegisterManager().getActiveRegister().getContent();
        TextContent content = editorAdaptor.getModelContent();
        int offset = editorAdaptor.getPosition().getModelOffset();
        LineInformation line = content.getLineInformationOfOffset(offset);
        try {
            final int startLine = line.getNumber();
            editorAdaptor.getHistory().beginCompoundChange();
            final TextBlockRegisterContent rect = (TextBlockRegisterContent) registerContent;
            int newCursorOfs = editorAdaptor.getPosition().addModelOffset(startOfs).getModelOffset();
            final int vWidth = rect.getVisualWidth();
            for (int i = 0; i < rect.getNumLines(); ++i) {
                Position pastePos;
                if (startLine + i >= content.getNumberOfLines() - 1) {
                    // Insert a new empty line if at the of the document.
                    content.replace(content.getTextLength(), 0, 
                            editorAdaptor.getConfiguration().getNewLine());
                }
                LineInformation pasteLine = content.getLineInformation(startLine + i);
                int vOffset = cursorService.getVisualOffset(editorAdaptor.getPosition());
                for (int c = 0; c < count; ++c) {
                    pastePos = cursorService.getPositionByVisualOffset(startLine + i, vOffset);
                    if (pastePos == null) {
                        pastePos = cursorService.newPositionForModelOffset(pasteLine.getEndOffset());
                        final int lineEndVOfs = cursorService.getVisualOffset(pastePos);
                        //
                        // "Extend" the paste line with spaces until it reaches vOffset.
                        //
                        final int padding = cursorService.visualWidthToChars(vOffset - lineEndVOfs);
                        content.replace(pasteLine.getEndOffset(), 0, StringUtils.multiply(" ", padding));
                        pastePos = pastePos.addModelOffset(padding);
                    }
                    // Refresh line information if extended.
                    pasteLine = content.getLineInformation(startLine + i);
                    if (startOfs > 0 && pasteLine.getEndOffset() == pastePos.getModelOffset()) {
                        content.replace(pasteLine.getEndOffset(), 0, " ");
                    }
                    //
                    // Paste after the character at vOffset.
                    //
                    final String blockLine = rect.getLine(i);
                    content.replace(pastePos.addModelOffset(startOfs).getModelOffset(), 0, blockLine);
                    //
                    // Right-pad with spaces if block-line is shorter and not at EOL.
                    //
                    pastePos = pastePos.addModelOffset(blockLine.length());
                    if (pastePos.getModelOffset() + startOfs < (pasteLine.getEndOffset() + blockLine.length()) || c != count - 1) {
                        final int vEndOfs = cursorService.getVisualOffset(pastePos);
                        final int padding = cursorService.visualWidthToChars(vOffset + vWidth - vEndOfs) + 1;
                        content.replace(pastePos.addModelOffset(startOfs).getModelOffset(), 0,
                                StringUtils.multiply(" ", padding));
                        pastePos = pastePos.addModelOffset(padding);
                    }
                    // Set vOffset for the next block line for count > 1.
                    vOffset = cursorService.getVisualOffset(pastePos);
                    if (placeCursorAfter) {
                        newCursorOfs = pastePos.getModelOffset() + startOfs;
                    }
                }
            }
            Position destination = cursorService.newPositionForModelOffset(newCursorOfs);
            editorAdaptor.setPosition(destination, StickyColumnPolicy.ON_CHANGE);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

}