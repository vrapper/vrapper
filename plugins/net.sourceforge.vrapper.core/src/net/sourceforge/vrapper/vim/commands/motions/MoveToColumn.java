package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class MoveToColumn extends LeftRightMotion {

    public static final MoveToColumn INSTANCE = new MoveToColumn();

    private MoveToColumn() { /* NOP */ }

    private EditorAdaptor adaptor;

    @Override
    protected void setCurrentState(EditorAdaptor editorAdaptor) {
        adaptor = editorAdaptor;
    }

    @Override
    protected int destination(int offset, TextContent content, int count) {
        LineInformation lineInfo = content.getLineInformationOfOffset(offset);
        int lineStart = lineInfo.getBeginOffset();
        int lineEnd = lineInfo.getEndOffset();
        String line = content.getText(lineStart, lineInfo.getLength());
        int tabstop = adaptor.getConfiguration().get(Options.TAB_STOP);
        int column = count - 1; // Columns start from 1, internal offsets from 0

        // We can't just take the n-th character, tabs have variable width. Find character for which
        // visual offset is greater than requested column, then previous char needs to be picked.
        int[] visualOffsets = StringUtils.calculateVisualOffsets(line, line.length(), tabstop);
        int charInLine = 0;
        while (charInLine < line.length() && visualOffsets[charInLine] <= column) {
            charInLine++;
        }
        // visualOffsets array size = line.length() + 1 so this works without exception
        if (visualOffsets[charInLine] > column) {
        	charInLine--;
        }
        return Math.min(lineEnd, lineStart+charInLine);
    }
}
