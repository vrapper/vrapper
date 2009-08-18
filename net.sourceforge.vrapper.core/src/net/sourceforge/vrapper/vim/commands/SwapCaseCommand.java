package net.sourceforge.vrapper.vim.commands;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;

public class SwapCaseCommand extends AbstractModelSideCommand {

    public static final SwapCaseCommand INSTANCE = new SwapCaseCommand();

    private SwapCaseCommand() { /* NOP */ }

    @Override
    protected int execute(TextContent content, int offset, int count) {
        LineInformation line = content.getLineInformationOfOffset(offset);
        int end = min(offset + count, line.getEndOffset());
        int length = end - offset;
        swapCase(content, offset, length);
        return end;
    }

    public static void swapCase(TextContent content, int start, int length) {
        String text = content.getText(start, length);
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < length; i++) {
            char c = text.charAt(i);
            c = isUpperCase(c) ? toLowerCase(c) : toUpperCase(c);
            s.append(c);
        }
        content.replace(start, length, s.toString());
    }

}