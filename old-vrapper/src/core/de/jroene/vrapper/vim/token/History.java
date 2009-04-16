package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * Defines {@link Token}s for undo and redo.
 *
 * @author Matthias Radig
 */
public abstract class History extends AbstractRepeatable {

    public static final Action UndoAction = new Action() {
        public void execute(VimEmulator vim) {
            vim.getPlatform().undo();
        }
    };

    public static final Action RedoAction = new Action() {
        public void execute(VimEmulator vim) {
            vim.getPlatform().redo();
        }
    };

    public Space getSpace(Token next) {
        return Space.MODEL;
    }

    public static class Undo extends History {
        @Override
        protected Action createAction() {
            return UndoAction;
        }
    }

    public static class Redo extends History {
        @Override
        protected Action createAction() {
            return RedoAction;
        }
    }
}