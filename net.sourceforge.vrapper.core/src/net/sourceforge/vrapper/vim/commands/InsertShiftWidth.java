package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class InsertShiftWidth extends CountIgnoringNonRepeatableCommand {

    public static final InsertShiftWidth INSTANCE = new InsertShiftWidth();
    
    private InsertShiftWidth() { /** no-op **/ }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        int tabstop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
        tabstop = Math.max(0, tabstop);
        int shiftwidth = editorAdaptor.getConfiguration().get(Options.SHIFT_WIDTH);
        shiftwidth = Math.max(0, shiftwidth);

        //I wish java could do (" " * tabstop)
        String replaceTab = "";
        while(replaceTab.length() < tabstop) {
            replaceTab += " ";
        }
        String replaceShiftWidth = "";
        while(replaceShiftWidth.length() < shiftwidth) {
            replaceShiftWidth += " ";
        }

        TextContent model = editorAdaptor.getModelContent();
        LineInformation line = model.getLineInformationOfOffset( editorAdaptor.getPosition().getModelOffset() );
        String lineStr = model.getText(line.getBeginOffset(), line.getLength());
        int whitespaceEnd = VimUtils.getFirstNonWhiteSpaceOffset(model, line) - line.getBeginOffset();

        String indent = lineStr.substring(0, whitespaceEnd);
        //expand all tab characters so we can recalculate tabstops
        indent = indent.replaceAll("\t", replaceTab);
        //introduce new indent
        indent = replaceShiftWidth + indent;

        //collapse tabstops back to tab characters
        String replace = "";
        while(indent.length() >= tabstop) {
            indent = indent.substring(tabstop);
            replace += "\t";
        }
        replace += indent; //preserve any non-shiftwidth divisible spaces
        model.replace(line.getBeginOffset(), whitespaceEnd, replace);
    }

}
