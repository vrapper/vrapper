package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.register.Register;
import de.jroene.vrapper.vim.register.RegisterContent;

/**
 * Inserts the content of the active register.
 *
 * @author Matthias Radig
 */
public class Put extends AbstractToken implements Repeatable {

    private final boolean preCursor;
    private int times;

    public Put(boolean preCursor) {
        super();
        this.preCursor = preCursor;
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        this.times = times;
        return true;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return repeat(vim, 1, next);
    }

    public Action getAction() {
        return new Action() {
            public void execute(VimEmulator vim) {
                Register reg = vim.getRegisterManager().getActiveRegister();
                RegisterContent content = reg.getContent();
                Platform p = vim.getPlatform();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < times; i++) {
                    if(content.isLineWise()) {
                        sb.append(VimConstants.NEWLINE);
                    }
                    sb.append(content.getPayload());
                }
                LineInformation line = p.getLineInformation();
                if (content.isLineWise()) {
                    if(!preCursor) {
                        int begin = line.getEndOffset();
                        if (line.getNumber() == p.getNumberOfLines()-1) {
                            begin += 1;
                        }
                        p.replace(begin, 0, sb.toString(), true);
                        p.setPosition(begin+1);
                    } else {
                        int begin = line.getBeginOffset();
                        sb.append(VimConstants.NEWLINE);
                        p.replace(begin, 0, sb.substring(1), true);
                        p.setPosition(begin);
                    }
                } else {
                    int position = p.getPosition();
                    if(!preCursor && position != line.getEndOffset()) {
                        position += 1;
                    }
                    String text = sb.toString();
                    p.replace(position, 0, text, true);
                    p.setPosition(position+text.length()-1);
                }
            }
        };
    }

    public Space getSpace() {
        return Space.MODEL;
    }

}
