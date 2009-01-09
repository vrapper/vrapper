package de.jroene.vrapper.vim;

import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.register.RegisterContent;
import de.jroene.vrapper.vim.token.AbstractToken;
import de.jroene.vrapper.vim.token.Number;
import de.jroene.vrapper.vim.token.Put;
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
            Platform p = vim.getPlatform();
            LineInformation line = p.getLineInformation();
            int position = p.getPosition();
            if(position > line.getBeginOffset()) {
                p.setPosition(position-1);
            }
            afterEdit();
            vim.toNormalMode();
        } else if(!allowed(e)) {
            clean = false;
        }
        return true;
    }

    private boolean allowed(VimInputEvent e) {
        return e instanceof VimInputEvent.Character
        || VimInputEvent.RETURN.equals(e);
    }
    private void afterEdit() {
        if (clean) {
            Platform platform = vim.getPlatform();
            int start = parameters.start;
            int end = platform.getPosition()+1;
            String input = platform.getText(start, end-start);
            RegisterContent content = new RegisterContent(parameters.lineWise, input);
            vim.getRegisterManager().getLastEditRegister().setContent(content);
            Token number = new Number(String.valueOf(parameters.times));
            try {
                number.evaluate(vim, new Put(parameters.preCursor));
            } catch (TokenException e) {
                throw new IllegalStateException(e);
            }
            Token token = new UseLastEditRegister(number);
            vim.getVariables().setLastEdit(token);
            token.getAction().execute(vim);
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

        public Parameters(boolean lineWise, boolean preCursor,
                int times, int start) {
            super();
            this.lineWise = lineWise;
            this.preCursor = preCursor;
            this.times = times;
            this.start = start;
        }

    }

    private static final class UseLastEditRegister extends AbstractToken {

        private final Token token;

        public UseLastEditRegister(Token token) {
            super();
            this.token = token;
        }

        public boolean evaluate(VimEmulator vim, Token next)
        throws TokenException {
            vim.getRegisterManager().activateLastEditRegister();
            return token.evaluate(vim, next);
        }

        public Action getAction() {
            return token.getAction();
        }

        public Space getSpace() {
            return token.getSpace();
        }

    }

}
