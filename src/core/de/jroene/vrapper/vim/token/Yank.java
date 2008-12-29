package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Yanks some text into the active register.
 *
 * @author Matthias Radig
 */
public class Yank extends AbstractLineAwareEdit {

    @Override
    public Action getAction() {
        return isLineDeletion() ? new LineYankAction() : new YankAction();
    }

    public class YankAction extends EditAction {
        @Override
        protected void doEdit(VimEmulator vim, int originalPosition, int start,
                int end) {
            // no change necessary
        }
    }

    public class LineYankAction extends LineEditAction {
        @Override
        protected void doEdit(VimEmulator vim, int originalPosition, int start,
                int end) {
            // no change necessary
        }
    }
}
