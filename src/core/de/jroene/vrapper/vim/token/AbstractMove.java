package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.action.HorizontalChangeMoveToAction;
import de.jroene.vrapper.vim.action.MoveToAction;

public abstract class AbstractMove extends AbstractToken implements Move {

    private int target;

    public AbstractMove() {
        super();
    }

    public int getTarget() {
        return target;
    }

    void setTarget(int target) {
        this.target = target;
    }

    protected abstract int calculateTarget(VimEmulator vim, Token next);
    public abstract boolean isHorizontal();

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        target = calculateTarget(vim, next);
        if (target == -1) {
            throw new TokenException();
        }
        return true;
    }

    public Action getAction() {
        return isHorizontal() ? new HorizontalChangeMoveToAction(target)
        : new MoveToAction(target);
    }

    public Space getSpace(Token next) {
        return Space.VIEW;
    }

    public boolean includesTarget() {
        return false;
    }

}