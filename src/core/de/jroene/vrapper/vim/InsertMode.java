package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.action.CompositeAction;
import de.jroene.vrapper.vim.register.RegisterContent;
import de.jroene.vrapper.vim.token.AbstractToken;
import de.jroene.vrapper.vim.token.Put;
import de.jroene.vrapper.vim.token.Repeatable;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;


/**
 * Insert Mode of the vim emulator. Returns to normal mode on press of escape.
 * Does not do anything else. (Keystrokes will be passed to the underlying editor)
 *
 * @author Matthias Radig
 */
public class InsertMode extends AbstractMode {

    private Parameters parameters;
    private boolean clean;

    public InsertMode(VimEmulator vim) {
        super(vim);
    }
    public boolean type(VimInputEvent e) {
        if(VimInputEvent.ESCAPE.equals(e)) {
            afterEdit();
            Platform p = vim.getPlatform();
            LineInformation line = p.getLineInformation();
            int position = p.getPosition();
            if(position > line.getBeginOffset()) {
                p.setPosition(position-1);
            }
            vim.toNormalMode();
            return false;
        }
        if(!allowed(e)) {
            vim.getPlatform().endChange();
            clean = false;
        }
        return true;
    }

    private boolean allowed(VimInputEvent e) {
        return e instanceof VimInputEvent.Character
        || VimInputEvent.RETURN.equals(e)
        || VimInputEvent.BACKSPACE.equals(e);
    }

    private void afterEdit() {
        Platform platform = vim.getPlatform();
        int start = parameters.start;
        int end = platform.getPosition();
        clean = clean && start < end;
        if (clean) {
            String input = platform.getText(start, end-start);
            RegisterContent content = new RegisterContent(parameters.lineWise, input);
            vim.getRegisterManager().getLastEditRegister().setContent(content);

            Repeatable token = new UseLastEditRegister(parameters.token, new Put(parameters.preCursor));
            Repeatable repeater = new DefaultRepeater(parameters.times, token);
            vim.getRegisterManager().setLastEdit(repeater);

            if (parameters.times > 1) {
                try {
                    repeater.repeat(vim, parameters.times-1, null);
                } catch (TokenException e) {
                    throw new IllegalStateException(e);
                }
                repeater.getAction().execute(vim);
            }
            vim.getPlatform().endChange();
        }
    }

    public void initializeWithParams(Parameters params) {
        parameters = params;
        clean = true;
    }

    public static class Parameters {

        private final boolean lineWise;
        private final boolean preCursor;
        private final int times;
        private final int start;
        private final Token token;

        public Parameters(boolean lineWise, boolean preCursor,
                int times, int start) {
            this(lineWise, preCursor, times, start, null);
        }

        public Parameters(boolean lineWise, boolean preCursor,
                int times, int start, Token token) {
            this.lineWise = lineWise;
            this.preCursor = preCursor;
            this.times = times;
            this.start = start;
            this.token = token;
        }

    }

    private static final class UseLastEditRegister extends AbstractToken implements Repeatable {

        private final Token first;
        private final Token second;

        public UseLastEditRegister(Token first, Token token) {
            super();
            this.second = token;
            this.first = first;
        }

        public boolean evaluate(VimEmulator vim, Token next)
        throws TokenException {
            vim.getRegisterManager().activateLastEditRegister();
            boolean result = true;
            if (first != null) {
                result = result && first.evaluate(vim, next);
            }
            return result && second.evaluate(vim, next);
        }

        public boolean repeat(VimEmulator vim, int times, Token next)
        throws TokenException {
            vim.getRegisterManager().activateLastEditRegister();
            if(second instanceof Repeatable) {
                boolean result = true;
                if (first != null) {
                    result = result && first.evaluate(vim, next);
                }
                return result && ((Repeatable)second).repeat(vim, times, next);
            } else {
                throw new TokenException();
            }
        }

        public Action getAction() {
            return first == null ? second.getAction() : new CompositeAction(first.getAction(), second.getAction());
        }

        public Space getSpace() {
            return second.getSpace();
        }

    }

}
