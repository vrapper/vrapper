package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

/**
 * Insert (or remove) &lt;shiftwidth&gt; spaces inside of a blockwise selection. Then
 * replace every &lt;tabstop&gt; spaces with a TAB character if &lt;expandtab&gt; is
 * disabled.
 */
public class BlockwiseInsertShiftWidth implements TextOperation {

    public static final TextOperation INSERT = new BlockwiseInsertShiftWidth(true);
    public static final TextOperation REMOVE = new BlockwiseInsertShiftWidth(false);

    private final boolean shiftRight;
    
    private BlockwiseInsertShiftWidth(boolean shiftRight) {
        this.shiftRight = shiftRight;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject) throws CommandExecutionException {
        int tabstop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
        tabstop = Math.max(1, tabstop);
        int shiftwidth = editorAdaptor.getConfiguration().get(Options.SHIFT_WIDTH);
        shiftwidth = Math.max(1, shiftwidth);
        boolean expandtab = editorAdaptor.getConfiguration().get(Options.EXPAND_TAB);
//      // Not used for blockwise shift?
//        boolean shiftround = editorAdaptor.getConfiguration().get(Options.SHIFT_ROUND);
        
        if (count != Counted.NO_COUNT_GIVEN) {
            shiftwidth = count * shiftwidth;
        }
        // Fill string with number of spaces.
        String replaceTab = new String(new char[tabstop]).replace('\0', ' ');
        String replaceShiftWidth = new String(new char[shiftwidth]).replace('\0', ' ');

        TextRange region = textObject.getRegion(editorAdaptor, Counted.NO_COUNT_GIVEN);
        TextContent model = editorAdaptor.getModelContent();
        LineInformation line = model.getLineInformationOfOffset(
                region.getLeftBound().getModelOffset());

        // Sanity check - each TextObject passed in should be just a single line part.
        if (line.getEndOffset() < region.getRightBound().getModelOffset()) {
            VrapperLog.error("Received incorrect shiftwidth segment! Start is at "
                + region.getLeftBound() + " with " + line + " but end is at "
                    + region.getRightBound());
            throw new CommandExecutionException("Failed to shift block, bad line found.");
        } else if (region.getModelLength() < 1) {
            VrapperLog.error("Received incorrect shiftwidth segment! Start is at "
                + region.getLeftBound() + " and segment length is 0!");
            throw new CommandExecutionException("Failed to shift block, bad line found.");
        }

        doIt(model, region, line, tabstop, shiftwidth, expandtab, replaceTab, replaceShiftWidth);
    }

    private void doIt(TextContent model, TextRange region,
            LineInformation line, int tabstop, int shiftwidth,
            boolean expandtab, String replaceTab, String replaceShiftWidth) {
        String contents = model.getText(line.getBeginOffset(), line.getLength());
        int leftOff = region.getLeftBound().getModelOffset() - line.getBeginOffset();
        int beginIndent = leftOff;
        int endIndent = leftOff;
        while (endIndent < contents.length()
                && VimUtils.isWhiteSpace(contents.substring(endIndent, endIndent + 1))) {
            endIndent++;
        }

        String indent;
        if (shiftRight) {
            // Check if there is indentation on the left, it might need tab expansion / coalescing.
            while (beginIndent > 0
                    && VimUtils.isWhiteSpace(contents.substring(beginIndent -1, beginIndent))) {
                beginIndent--;
            }
            indent = contents.substring(beginIndent, endIndent);
            //expand all tab characters so we can recalculate tabstops
            indent = indent.replaceAll("\t", replaceTab);
            indent = replaceShiftWidth + indent;

        } else if (VimUtils.isWhiteSpace(contents.substring(leftOff, leftOff + 1))) {
            indent = contents.substring(leftOff, endIndent);
            //expand all tab characters so we can recalculate tabstops
            indent = indent.replaceAll("\t", replaceTab);
            //remove an indent - shiftround is not used in blockmode
            if (shiftwidth > indent.length()) {
                indent = "";
            } else {
                indent = indent.substring(shiftwidth);
            }
        } else {
            // If there is no whitespace at the start of the block, nothing needs to be done!
            return;
        }

        String replace = "";
        if( ! expandtab) {
            //collapse <tabstop> spaces into tab characters
            while(indent.length() >= tabstop) {
                indent = indent.substring(tabstop);
                replace += "\t";
            }
        }
        replace += indent;
        model.replace(line.getBeginOffset() + beginIndent, endIndent - beginIndent, replace);
    }

    @Override
    public TextOperation repetition() {
        return this;
    }

}
