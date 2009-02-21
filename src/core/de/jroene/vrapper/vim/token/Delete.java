package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Deletes a region of text.
 *
 * @author Matthias Radig
 */
public class Delete extends AbstractLineAwareEdit {

    public Delete() {
        super();
    }

    public Delete(int target, Move subject, Number multiplier) {
        super(target, subject, multiplier);
    }

    @Override
    public Action getAction() {
        return isLineDeletion() ? new LineDeleteAction() : new DeleteAction();
    }

    public class LineDeleteAction extends LineEditAction {
        @Override
        protected void doEdit(VimEmulator vim, int originalPosition, int start,
                int end) {
            Platform pl = vim.getPlatform();
            pl.replace(start, end - start, "");
            pl.setPosition(originalPosition);
        }
    }

    public class DeleteAction extends EditAction {
        @Override
        protected void doEdit(VimEmulator vim, int originalPosition, int start,
                int end) {
            vim.getPlatform().replace(start, end - start, "");
        }
    }

    static class BufferNeutral extends Delete {

        public BufferNeutral() {
            super();
        }

        public BufferNeutral(int target, Move subject, Number multiplier) {
            super(target, subject, multiplier);
        }

        @Override
        public Action getAction() {
            return isLineDeletion() ? new NeutralLineDeleteAction() : new NeutralDeleteAction();
        }

        private class NeutralLineDeleteAction extends LineDeleteAction {

            @Override
            protected void beforeEdit(VimEmulator vim, LineInformation start, LineInformation end) {
                // do nothing
            }
        }

        private class NeutralDeleteAction extends DeleteAction {

            @Override
            protected void beforeEdit(VimEmulator vim, int start, int end) {
                // do nothing
            }
        }
    }

}
