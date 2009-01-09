package de.jroene.vrapper.vim.action;

import de.jroene.vrapper.vim.InsertMode;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.InsertMode.Parameters;
import de.jroene.vrapper.vim.token.BeginOfLineMove;
import de.jroene.vrapper.vim.token.EndOfLineMove;
import de.jroene.vrapper.vim.token.Move;
import de.jroene.vrapper.vim.token.Repeatable;
import de.jroene.vrapper.vim.token.RightMove;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

/**
 * Switches to insert mode.
 *
 * @author Matthias Radig
 */
public abstract class InsertModeAction extends TokenAndAction implements Repeatable {

    private final Move move;
    private int times;

    public InsertModeAction(Move move) {
        super();
        this.move = move;
        this.times = 1;
    }

    public void execute(VimEmulator vim) {
        if (move != null) {
            try {
                move.evaluate(vim, null);
            } catch (TokenException e) {
                throw new IllegalStateException(e);
            }
            move.getAction().execute(vim);
        }
        vim.toInsertMode(getParameters(vim, times));
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        this.times = times;
        return true;
    }

    abstract Parameters getParameters(VimEmulator vim, int times);

    public static class Insert extends InsertModeAction {

        public Insert() {
            super(null);
        }

        @Override
        Parameters getParameters(VimEmulator vim, int times) {
            return new InsertMode.Parameters(false, true, times, vim.getPlatform().getPosition());
        }
    }

    public static class Append extends InsertModeAction {

        private static final Move move = new RightMove();

        public Append() {
            super(move);
        }

        @Override
        Parameters getParameters(VimEmulator vim, int times) {
            return new InsertMode.Parameters(false, false, times, vim.getPlatform().getPosition());
        }
    }

    public static class BeginOfLineInsert extends InsertModeAction {

        private static final Move move = new BeginOfLineMove.FirstText();

        public BeginOfLineInsert() {
            super(move);
        }

        @Override
        Parameters getParameters(VimEmulator vim, int times) {
            int start = vim.getPlatform().getPosition();
            return new InsertMode.Parameters(false, true, times, start, move);
        }
    }

    public static class EndOfLineAppend extends InsertModeAction {

        private static final Move move = new EndOfLineMove();

        public EndOfLineAppend() {
            super(move);
        }

        @Override
        Parameters getParameters(VimEmulator vim, int times) {
            int start = vim.getPlatform().getPosition();
            return new InsertMode.Parameters(false, false, times, start, new EndOfLineMove());
        }

    }

}
