package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

/**
 * Insert (or remove) <shiftwidth> spaces at the beginning of the line. Then
 * replace every <tabstop> spaces with a TAB character if <expandtab> is
 * disabled.
 */
public class InsertShiftWidth extends CountIgnoringNonRepeatableCommand {

    public static final InsertShiftWidth INSERT = new InsertShiftWidth(true);
    public static final InsertShiftWidth REMOVE = new InsertShiftWidth(false);
    
    private final boolean insert;
    
    private InsertShiftWidth(boolean insert) {
        this.insert = insert;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        int tabstop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
        tabstop = Math.max(1, tabstop);
        int shiftwidth = editorAdaptor.getConfiguration().get(Options.SHIFT_WIDTH);
        shiftwidth = Math.max(1, shiftwidth);

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

        if(insert) {
            //introduce new indent
            indent = replaceShiftWidth + indent;
        }
        else {
            //remove an indent
            indent = indent.substring(shiftwidth);
        }

        String replace = "";
        if( ! editorAdaptor.getConfiguration().get(Options.EXPAND_TAB)) {
            //collapse <tabstop> spaces into tab characters
            while(indent.length() >= tabstop) {
                indent = indent.substring(tabstop);
                replace += "\t";
            }
        }
        replace += indent; //preserve any non-tabstop divisible spaces
        model.replace(line.getBeginOffset(), whitespaceEnd, replace);
    }

}
