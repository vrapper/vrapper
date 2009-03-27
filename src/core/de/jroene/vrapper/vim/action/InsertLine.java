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
            String newline = vim.getVariables().getNewLine();
            
            if(currentLine.getNumber() != 0) {
                int index = currentLine.calculateAboveEndOffset(p);
                p.setPosition(index);
                p.insert(newline);
                p.setPosition(p.getLineInformation(p.getLineInformation().getNumber()+1).getEndOffset());
            } else {
                p.setPosition(0);
                p.insert("\n");
                p.setPosition(0);
            }
        }

        @Override
        InsertMode.Parameters getParameters(LineInformation line, int times) {
            return new InsertMode.Parameters(true, true, times, line.getBeginOffset());
        }
    }

    public static class PostCursor extends InsertLine {

        @Override
        protected void doEdit(VimEmulator vim, LineInformation currentLine, String indent) {
            String newline = vim.getVariables().getNewLine();
            Platform p = vim.getPlatform();
            
            int begin = currentLine.getEndOffset();
            p.setPosition(begin);
            p.insert(newline);
            p.setPosition(p.getLineInformation(p.getLineInformation().getNumber()+1).getEndOffset());
        }

        @Override
        InsertMode.Parameters getParameters(LineInformation line, int times) {
            return new InsertMode.Parameters(true, false, times, line.getEndOffset()+1);
        }

    }
}
