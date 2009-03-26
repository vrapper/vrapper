package de.jroene.vrapper.vim.token;


import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.Action;

/**
 * Shifts a block of text.
 *
 * @author Matthias Radig
 */
public class Shift extends AbstractLineAwareToken {

    private final int modifier;

    public Shift(int modifier) {
        super();
        this.modifier = modifier;
    }

    @Override
    public Action getAction() {
        return new ShiftAction(getTarget());
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    public class ShiftAction implements Action {

        private int target;

        public ShiftAction(int target) {
            super();
            this.target = target;
        }

        public void execute(VimEmulator vim) {
            Platform p = vim.getPlatform();
            int position = p.getPosition();
            if (target == -1) {
                target = position;
            }
            LineInformation start;
            LineInformation end;
            LineInformation line = p.getLineInformation();
            if (position < target) {
                start = line;
                end = p.getLineInformationOfOffset(target);
            } else {
                end = line;
                start = p.getLineInformationOfOffset(target);
            }
            int count = end.getNumber() - start.getNumber() + 1;
            p.shift(start.getNumber(), count, modifier);
            // fetch new line info
            line = p.getLineInformation(line.getNumber());
            p.setPosition(VimUtils.getFirstNonWhiteSpaceOffset(vim, line));
        }

    }
}
