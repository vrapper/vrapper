package de.jroene.vrapper.vim.token;


import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Shifts a block of text.
 *
 * @author Matthias Radig
 */
public class Shift extends AbstractLineAwareToken implements Repeatable {

    private final int modifier;

    public Shift(int modifier) {
        super();
        this.modifier = modifier;
    }

    @Override
    public Action getAction() {
        return new ShiftAction(isLineDeletion() ? getTarget() : -1);
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
            if (position < target) {
                start = p.getLineInformation();
                end = p.getLineInformationOfOffset(target);
            } else {
                end = p.getLineInformation();
                start = p.getLineInformationOfOffset(target);
            }
            int count = end.getNumber() - start.getNumber() + 1;
            p.shift(start.getNumber(), count, modifier);
        }

    }
}
