package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
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
                LineInformation pasteLine = content.getLineInformation(startLine + i);
                int vOffset = cursorService.getVisualOffset(editorAdaptor.getPosition());
                for (int c = 0; c < count; ++c) {
                    //
                    // "Extend" the paste line with spaces until it reaches vOffset.
                    //
                    while ((pastePos = cursorService.getPositionByVisualOffset(startLine + i, vOffset)) == null) {
                        content.replace(pasteLine.getEndOffset(), 0, " ");
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
                    // Right-pad with spaces if block-line is shorter.
                    //
                    pastePos = pastePos.addModelOffset(blockLine.length());
                    while (cursorService.getVisualOffset(pastePos) <= vOffset + vWidth) {
                        content.replace(pastePos.addModelOffset(startOfs).getModelOffset(), 0, " ");
                        pastePos = pastePos.addModelOffset(1);
                    }
                    // Set vOffset for the next block line for count > 1.
                    vOffset = cursorService.getVisualOffset(pastePos);
                    if (placeCursorAfter) {
                        newCursorOfs = pastePos.getModelOffset() + startOfs;
                    }
                }
            }
            Position destination = cursorService.newPositionForModelOffset(newCursorOfs);
            editorAdaptor.setPosition(destination, true);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

}