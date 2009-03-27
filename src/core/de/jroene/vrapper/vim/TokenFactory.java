package de.jroene.vrapper.vim;

import java.util.HashMap;

import de.jroene.vrapper.vim.action.CommandLineAction;
import de.jroene.vrapper.vim.action.InsertLine;
import de.jroene.vrapper.vim.action.InsertModeAction;
import de.jroene.vrapper.vim.action.RepeatLastEdit;
import de.jroene.vrapper.vim.action.SearchModeAction;
import de.jroene.vrapper.vim.action.SearchMove;
import de.jroene.vrapper.vim.action.VisualModeAction;
import de.jroene.vrapper.vim.action.WordSearchMove;
import de.jroene.vrapper.vim.token.BeginOfLineMove;
import de.jroene.vrapper.vim.token.Change;
import de.jroene.vrapper.vim.token.CompositeToken;
import de.jroene.vrapper.vim.token.Delete;
import de.jroene.vrapper.vim.token.DownMove;
import de.jroene.vrapper.vim.token.EndOfLineMove;
import de.jroene.vrapper.vim.token.FindMove;
import de.jroene.vrapper.vim.token.GotoMove;
import de.jroene.vrapper.vim.token.History;
import de.jroene.vrapper.vim.token.Join;
import de.jroene.vrapper.vim.token.LeftMove;
import de.jroene.vrapper.vim.token.Number;
import de.jroene.vrapper.vim.token.ParenthesesMove;
import de.jroene.vrapper.vim.token.Put;
import de.jroene.vrapper.vim.token.RepeatFindMove;
import de.jroene.vrapper.vim.token.Replace;
import de.jroene.vrapper.vim.token.RightMove;
import de.jroene.vrapper.vim.token.Scroll;
import de.jroene.vrapper.vim.token.Shift;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.UpMove;
import de.jroene.vrapper.vim.token.UseRegister;
import de.jroene.vrapper.vim.token.WordMove;
import de.jroene.vrapper.vim.token.Yank;

/**
 * Produces tokens according to input events.
 *
 * @author Matthias Radig
 */
public class TokenFactory {

    private static final HashMap<VimInputEvent, Token> tokens;
    static {
        tokens = new HashMap<VimInputEvent, Token>();
        // Movement keys
        Token left = new LeftMove();
        Token right = new RightMove();
        Token up = new UpMove();
        Token down = new DownMove();
        put('h', left);
        put('l', right);
        put('j', down);
        put('k', up);
        tokens.put(VimInputEvent.ARROW_LEFT, left);
        tokens.put(VimInputEvent.ARROW_RIGHT, right);
        tokens.put(VimInputEvent.ARROW_DOWN, down);
        tokens.put(VimInputEvent.ARROW_UP, up);
        // line and page movement
        put('g', new GotoMove(false));
        put('G', new GotoMove(true));
        put('\u0006', new Scroll(false));
        put('\u0002', new Scroll(true));
        tokens.put(VimInputEvent.PAGE_DOWN, new Scroll(false));
        tokens.put(VimInputEvent.PAGE_UP, new Scroll(true));
        // word movement
        put('w', new WordMove.NextBegin(VimConstants.WORD_CHAR_PATTERN));
        put('e', new WordMove.NextEnd(VimConstants.WORD_CHAR_PATTERN));
        put('b', new WordMove.LastBegin(VimConstants.WORD_CHAR_PATTERN));
        put('W', new WordMove.NextBegin(""));
        put('E', new WordMove.NextEnd(""));
        put('B', new WordMove.LastBegin(""));
        // complex movement
        BeginOfLineMove beginOfLine = new BeginOfLineMove.FirstText();
        put('^', beginOfLine);
        tokens.put(VimInputEvent.HOME, beginOfLine);
        EndOfLineMove endOfLine = new EndOfLineMove();
        put('$', endOfLine);
        tokens.put(VimInputEvent.END, endOfLine);
        put('f', new FindMove(false, false));
        put('F', new FindMove(true, false));
        put('T', new FindMove(true, true));
        put('t', new FindMove(false, true));
        put(';', new RepeatFindMove(false));
        put(',', new RepeatFindMove(true));
        put('%', new ParenthesesMove());
        // Insert Actions
        put('i', new InsertModeAction.Insert());
        put('a', new InsertModeAction.Append());
        put('I', new InsertModeAction.BeginOfLineInsert());
        put('A', new InsertModeAction.EndOfLineAppend());
        put('r', new Replace());
        put('O', new InsertLine(InsertLine.Type.PRE_CURSOR));
        put('o', new InsertLine(InsertLine.Type.POST_CURSOR));
        // numbers / multipliers
        for(int i = 0; i < 9; i++) {
            String digit = String.valueOf(i);
            put(digit.charAt(0), new Number(digit));
        }
        put('0', new BeginOfLineMove.Absolute());
        // delete, yank, put
        Delete delete = new Delete();
        CompositeToken delRight = new CompositeToken(delete, right);
        put('x', delRight);
        tokens.put(VimInputEvent.DELETE, delRight);
        put('X', new CompositeToken(delete, left));
        put('d', delete);
        put('D', new CompositeToken(delete, endOfLine));
        Change change = new Change();
        put('c', change);
        put('C', new CompositeToken(change, endOfLine));
        put('s', new CompositeToken(change, right));
        put('S', new CompositeToken(change, change));
        Yank yank = new Yank();
        put('y', yank);
        put('Y', new CompositeToken(yank, yank));
        Put put = new Put(false);
        put('p', put);
        tokens.put(VimInputEvent.INSERT, put);
        put('P', new Put(true));
        put('"', new UseRegister());
        // other edits
        put('>', new Shift(1));
        put('<', new Shift(-1));
        put('J', new Join());
        put('.', new RepeatLastEdit());
        // history
        put('u', new History.Undo());
        put('U', new History.Redo());
        // command line / search
        put(':', new CommandLineAction());
        put('/', new SearchModeAction(false));
        put('?', new SearchModeAction(true));
        put('n', new SearchMove(false));
        put('N', new SearchMove(true));
        put('*', new WordSearchMove(false));
        put('#', new WordSearchMove(true));
        // visual mode
        put('v', new VisualModeAction(false));
        put('V', new VisualModeAction(true));
    }

    public static final Token create(VimInputEvent e) {
        if(tokens.containsKey(e)) {
            return tokens.get(e).clone();
        }
        return null;
    }

    private static void put(char c, Token t) {
        tokens.put(new VimInputEvent.Character(c), t);
    }
}
