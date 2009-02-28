package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.InsertMode;
import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.token.Repeatable;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

/**
 * Inserts a new line and switches to insert mode.
 *
 * @author Matthias Radig
 */
public abstract class InsertLine extends TokenAndAction implements Repeatable {

    private int times = 1;

    public final void execute(VimEmulator vim) {
        Platform p = vim.getPlatform();
        LineInformation line = p.getLineInformation();
        String indent = vim.getVariables().isAutoIndent() ? VimUtils.getIndent(vim, line) : "";
        doEdit(vim, line, indent);
        vim.toInsertMode(getParameters(line, times));
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        this.times = times;
        return true;
    }

    abstract InsertMode.Parameters getParameters(LineInformation line, int times);

    protected abstract void doEdit(VimEmulator vim, LineInformation line, String indent);

    public static class PreCursor extends InsertLine {

        @Override
        protected void doEdit(VimEmulator vim, LineInformation currentLine, String indent) {
            Platform p = vim.getPlatform();
            String newline = vim.getVariables().getNewLine().nl;
            p.replace(currentLine.getBeginOffset(), 0, indent+newline);
            p.setPosition(currentLine.getBeginOffset()+indent.length());
        }

        @Override
        InsertMode.Parameters getParameters(LineInformation line, int times) {
            return new InsertMode.Parameters(true, true, times, line.getBeginOffset());
        }
    }

    public static class PostCursor extends InsertLine {

        @Override
        protected void doEdit(VimEmulator vim, LineInformation currentLine, String indent) {
            Platform p = vim.getPlatform();
            int begin = currentLine.getEndOffset();
            if (currentLine.getNumber() == p.getNumberOfLines()-1) {
                // there is a character at the end offset, which belongs to the line
                begin += 1;
            }
            String newline = vim.getVariables().getNewLine().nl;
            p.replace(begin, 0, newline+indent);
            p.setPosition(begin+indent.length()+newline.length());
        }

        @Override
        InsertMode.Parameters getParameters(LineInformation line, int times) {
            return new InsertMode.Parameters(true, false, times, line.getEndOffset()+1);
        }

    }
}
