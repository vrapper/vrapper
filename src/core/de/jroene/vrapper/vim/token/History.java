package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Defines {@link Token}s for undo and redo.
 *
 * @author Matthias Radig
 */
public class History {

    public static class Undo extends AbstractRepeatable {
        @Override
        protected Action createAction() {
            return new Action() {
                public void execute(VimEmulator vim) {
                    vim.getPlatform().undo();
                }
            };
        }

        public Space getSpace(Token next) {
            return Space.MODEL;
        }
    }

    public static class Redo extends AbstractRepeatable {
        @Override
        protected Action createAction() {
            return new Action() {
                public void execute(VimEmulator vim) {
                    vim.getPlatform().redo();
                }
            };
        }

        public Space getSpace(Token next) {
            return Space.MODEL;
        }
    }
}