package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.action.HorizontalChangeMoveToAction;

/**
 * Move to the begin of the line.
 *
 * @author Matthias Radig
 */
public abstract class BeginOfLineMove extends AbstractMove implements Move {

    @Override
    public boolean isHorizontal() {
        return true;
    }

    @Override
    public boolean includesTarget() {
        return true;
    }

    /**
     * Moves to the "real" begin of the line. It also extends Number, because
     * it is mapped to the 0-key which is also used for specifying a count. This
     * is quite a hack.
     *
     * @author Matthias Radig
     */
    public static class Absolute extends Number implements Move {

        private int target = -1;

        public Absolute() {
            super("0");
        }

        public int getTarget() {
            return target;
        }

        public boolean isHorizontal() {
            return true;
        }

        @Override
        public boolean isOperator() {
            return target == -1 && super.isOperator();
        }

        @Override
        public boolean evaluate(VimEmulator vim, Token next)
        throws TokenException {
            target = vim.getPlatform().getLineInformation().getBeginOffset();
            return true;
        }

        @Override
        public Action getAction() {
            return new HorizontalChangeMoveToAction(target);
        }

        @Override
        public Space getSpace() {
            return Space.VIEW;
        }

        public boolean includesTarget() {
            return true;
        }
    }

    /**
     * Moves to the first non-whitespace character of the line, or the end of
     * the line if the line is blank.
     *
     * @author Matthias Radig
     */
    public static class FirstText extends BeginOfLineMove {

        @Override
        protected int calculateTarget(VimEmulator vim, Token next) {
            return VimUtils.getFirstNonWhiteSpaceOffset(vim, vim.getPlatform().getLineInformation());
        }

    }
}
