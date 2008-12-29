package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Deletes a region of text.
 *
 * @author Matthias Radig
 */
public class Delete extends AbstractLineAwareEdit implements Repeatable {

    @Override
    public Action getAction() {
        return isLineDeletion() ? new LineDeleteAction() : new DeleteAction();
    }

    public class LineDeleteAction extends LineEditAction {
        @Override
        protected void doEdit(VimEmulator vim, int originalPosition, int start,
                int end) {
            Platform pl = vim.getPlatform();
            pl.replace(start, end - start, "", false);
            pl.setPosition(originalPosition);
        }
    }

    public class DeleteAction extends EditAction {
        @Override
        protected void doEdit(VimEmulator vim, int originalPosition, int start,
                int end) {
            vim.getPlatform().replace(start, end - start, "", false);
        }
    }

}
