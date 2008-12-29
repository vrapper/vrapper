package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

/**
 * Inserts a new line and switches to insert mode.
 *
 * @author Matthias Radig
 */
public abstract class InsertLine extends TokenAndAction {

    public final void execute(VimEmulator vim) {
        Platform p = vim.getPlatform();
        LineInformation line = p.getLineInformation();
        String indent = vim.getVariables().isAutoIndent() ? VimUtils.getIndent(vim, line) : "";
        doEdit(p, line, indent);
        vim.toInsertMode();
    }

    protected abstract void doEdit(Platform p, LineInformation line, String indent);

    public static class PreCursor extends InsertLine {

        @Override
        protected void doEdit(Platform p, LineInformation currentLine, String indent) {
            p.replace(currentLine.getBeginOffset(), 0, indent+VimConstants.NEWLINE, true);
            p.setPosition(currentLine.getBeginOffset()+indent.length());
        }
    }

    public static class PostCursor extends InsertLine {

        @Override
        protected void doEdit(Platform p, LineInformation currentLine, String indent) {
            int begin = currentLine.getEndOffset();
            if (currentLine.getNumber() == p.getNumberOfLines()-1) {
                begin += 1;
            }
            p.replace(begin, 0, VimConstants.NEWLINE+indent, true);
            p.setPosition(begin+indent.length()+1);
        }

    }
}
