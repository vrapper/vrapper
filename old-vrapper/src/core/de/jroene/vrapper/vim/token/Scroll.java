package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.ViewPortInformation;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.Action;

/**
 * Scrolls the viewport.
 *
 * @author Matthias Radig
 */
public class Scroll extends AbstractToken implements Repeatable {

    private final boolean up;
    private int target;
    private int newTop;

    public Scroll(boolean up) {
        super();
        this.up = up;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return repeat(vim, 1, next);
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        Platform p = vim.getPlatform();
        ViewPortInformation view = p.getViewPortInformation();
        int nol = view.getNumberOfLines();
        int top = view.getTopLine();
        int bottom = view.getBottomLine();
        int end = p.getNumberOfLines()-1;
        if (nol >= p.getNumberOfLines() || up && top == 0 || !up && bottom == end) {
            throw new TokenException();
        }
        int cursorLine;
        if (up) {
            newTop = Math.max(0, top-nol*times+1);
            cursorLine = newTop+nol;
        } else {
            newTop = Math.min(end, top+nol*times-1);
            cursorLine = newTop;
        }
        target = VimUtils.getSOLAwarePositionAtLine(vim, cursorLine);
        return true;
    }

    public Action getAction() {
        return new Action() {
            public void execute(VimEmulator vim) {
                Platform p = vim.getPlatform();
                p.setTopLine(newTop);
                p.setPosition(target);
            }
        };
    }

    public Space getSpace(Token next) {
        return Space.VIEW;
    }
}
