package de.jroene.vrapper.vim;

import java.util.HashMap;

import de.jroene.vrapper.vim.token.KeyStrokeToken;
import de.jroene.vrapper.vim.token.Token;
import de.jroene.vrapper.vim.token.TokenException;

/**
 * Normal mode of the vim emulator. Most important and most complex mode.
 *
 * @author Matthias Radig
 */
public class NormalMode extends AbstractMode {

    private final HashMap<VimInputEvent, VimInputEvent> keyMappings;
    private Token startToken;

    public NormalMode(VimEmulator vim) {
        super(vim);
        keyMappings = new HashMap<VimInputEvent, VimInputEvent>();
    }

    private NormalMode getParent() {
        return this;
    }

    public Mode getKeystrokeMode() {
        return new KeyStrokeMode();
    }

    public boolean type(VimInputEvent e) {
        if (keyMappings.containsKey(e)) {
            e = keyMappings.get(e);
        }
        Token t = TokenFactory.create(e);
        processToken(t);
        return false;
    }

    public void overrideMapping(char c, char d) {
        if (c == d) {
            keyMappings.remove(new VimInputEvent.Character(c));
        } else {
            keyMappings.put(new VimInputEvent.Character(c),
                    new VimInputEvent.Character(d));
        }
    }

    private void processToken(Token t) {
        vim.getRegisterManager().activateDefaultRegister();
        if (t != null) {
            try {
                if (startToken == null ) {
                    startToken = t;
                    t = null;
                }
                vim.getPlatform().setSpace(startToken.getSpace());
                if (startToken.evaluate(vim, t)) {
                    startToken.getAction().execute(vim);
                    if(vim.inNormalMode()) {
                        afterExecute();
                    }
                    cleanUp();
                }
            } catch (TokenException ex) {
                cleanUp();
            }
        }
        vim.getPlatform().setDefaultSpace();
    }

    void cleanUp() {
        startToken = null;
    }

    private class KeyStrokeMode implements Mode {

        public boolean type(VimInputEvent e) {
            processToken(KeyStrokeToken.from(e));
            vim.toNormalMode();
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return getParent().equals(obj);
        }

        @Override
        public int hashCode() {
            return getParent().hashCode();
        }

    }

    private void afterExecute() {
        // check position - end of a line NOT allowed if length > 0
        Platform platform = vim.getPlatform();
        LineInformation line = platform.getLineInformation();
        int position = platform.getPosition();
        if(position > line.getEndOffset()-1 && line.getLength() > 0) {
            position -= 1;
            platform.setPosition(position);
        }
    }
}
