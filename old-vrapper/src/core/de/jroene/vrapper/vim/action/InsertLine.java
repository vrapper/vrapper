package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.InsertMode;
import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.InsertMode.Parameters;
import de.jroene.vrapper.vim.token.Repeatable;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

/**
 * Inserts a new line and switches to insert mode.
 * 
 * @author Matthias Radig
 */
public final class InsertLine extends TokenAndAction implements Repeatable {
    public static enum Type {
        PRE_CURSOR {
            @Override
            void dumb(VimEmulator vim, LineInformation line, String indent) {
                Platform p = vim.getPlatform();
                String newline = vim.getVariables().getNewLine();
                p.replace(line.getBeginOffset(), 0, indent + newline);
                p.setPosition(line.getBeginOffset() + indent.length());
            }

            @Override
            void smart(VimEmulator vim, LineInformation line) {
                Platform p = vim.getPlatform();
                String newline = vim.getVariables().getNewLine();

                if (line.getNumber() != 0) {
                    int index = line.calculateAboveEndOffset(p);
                    p.setPosition(index);
                    p.insert(newline);
                    p.setPosition(p.getLineInformation(
                            p.getLineInformation().getNumber() + 1)
                            .getEndOffset());
                } else {
                    p.setPosition(0);
                    p.insert("\n");
                    p.setPosition(0);
                }
            }

            @Override
            Parameters getParameters(LineInformation line, int times) {
                return new InsertMode.Parameters(true, true, times, line
                        .getBeginOffset());
            }
        },
        POST_CURSOR {
            @Override
            void dumb(VimEmulator vim, LineInformation line, String indent) {
                Platform p = vim.getPlatform();
                int begin = line.getEndOffset();
                if (line.getNumber() == p.getNumberOfLines()-1) {
                    // there is a character at the end offset, which belongs to the line
                    begin += 1;
                }
                String newline = vim.getVariables().getNewLine();
                p.replace(begin, 0, newline+indent);
                p.setPosition(begin+indent.length()+newline.length());
            }

            @Override
            void smart(VimEmulator vim, LineInformation line) {
                String newline = vim.getVariables().getNewLine();
                Platform p = vim.getPlatform();

                int begin = line.getEndOffset();
                p.setPosition(begin);
                p.insert(newline);
                p.setPosition(p.getLineInformation(
                        p.getLineInformation().getNumber() + 1).getEndOffset());
            }

            @Override
            Parameters getParameters(LineInformation line, int times) {
                return new InsertMode.Parameters(true, false, times, line
                        .getEndOffset() + 1);
            }
        };

        abstract void smart(VimEmulator vim, LineInformation line);

        abstract void dumb(VimEmulator vim, LineInformation line, String indent);

        abstract InsertMode.Parameters getParameters(LineInformation line,
                int times);
    }

    private final Type type;

    private int times = 1;

    public InsertLine(InsertLine.Type type) {
        this.type = type;
    }

    public final void execute(VimEmulator vim) {
        Platform p = vim.getPlatform();
        LineInformation line = p.getLineInformation();
        if (vim.getVariables().isSmartIndent()) {
            this.type.smart(vim, line);
        } else {
            String indent = vim.getVariables().isAutoIndent() ? VimUtils
                    .getIndent(vim, line) : "";
            this.type.dumb(vim, line, indent);
        }

        vim.toInsertMode(this.type.getParameters(line, times));
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
            throws TokenException {
        this.times = times;
        return true;
    }
}
