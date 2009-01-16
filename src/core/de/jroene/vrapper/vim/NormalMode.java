package de.jroene.vrapper.vim;

import java.util.HashMap;

import de.jroene.vrapper.vim.token.AbstractLineAwareToken;
import de.jroene.vrapper.vim.token.CompositeToken;
import de.jroene.vrapper.vim.token.KeyStrokeToken;
import de.jroene.vrapper.vim.token.Move;
import de.jroene.vrapper.vim.token.Number;
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

    public Mode getVisualMode(boolean lineWise) {
        return new VisualMode(lineWise);
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
        Platform platform = vim.getPlatform();
        if (t != null) {
            try {
                if (startToken == null ) {
                    startToken = t;
                    t = null;
                    if (platform.getSelection() != null && startToken instanceof AbstractLineAwareToken) {
                        t = platform.getSelection();
                        vim.toNormalMode();
                    }
                }
                platform.setSpace(startToken.getSpace());
                if (startToken.evaluate(vim, t)) {
                    startToken.getAction().execute(vim);
                    if(startToken.isOperator()) {
                        Token tok = startToken;
                        if (tok instanceof Number) {
                            tok = ((Number)tok).asDefaultRepeater();
                        }
                        CompositeToken change = new CompositeToken(tok, t);
                        vim.getVariables().setLastEdit(change);
                    }
                    if(vim.inNormalMode()) {
                        afterExecute();
                    }
                    cleanUp();
                }
            } catch (TokenException ex) {
                cleanUp();
            }
        }
        platform.setDefaultSpace();
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

    private class VisualMode implements Mode {

        private final int start;
        private final boolean lineWise;

        public VisualMode(boolean lineWise) {
            super();
            this.lineWise = lineWise;
            int pos = vim.getPlatform().getPosition();
            if (lineWise) {
                start = vim.getPlatform().getLineInformationOfOffset(pos).getBeginOffset();
            } else {
                this.start = pos;
            }
        }

        public boolean type(VimInputEvent e) {
            Platform platform = vim.getPlatform();
            if (keyMappings.containsKey(e)) {
                e = keyMappings.get(e);
            }
            if (VimInputEvent.ESCAPE.equals(e)) {
                platform.setSelection(null);
                vim.toNormalMode();
            }
            Token t = TokenFactory.create(e);
            if (t instanceof Move || t instanceof Number || t instanceof AbstractLineAwareToken) {
                processToken(t);
            }
            int end;
            int pos = platform.getPosition();
            if (lineWise) {
                end = platform.getLineInformationOfOffset(pos).getEndOffset();
            } else {
                end = pos;
            }
            platform.setSelection(Selection.fromOffsets(start, end));
            return false;
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
