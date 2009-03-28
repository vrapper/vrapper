package de.jroene.vrapper.vim;

import java.util.HashMap;

import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.action.CommandLineAction;
import de.jroene.vrapper.vim.action.SearchModeAction;
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
    private boolean keyStrokeMode;

    public NormalMode(VimEmulator vim) {
        super(vim);
        keyMappings = new HashMap<VimInputEvent, VimInputEvent>();
        keyStrokeMode = false;
    }

    public void toKeystrokeMode() {
        keyStrokeMode = true;
    }

    public VisualMode getVisualMode(boolean lineWise) {
        return new VisualMode(lineWise);
    }

    public boolean type(VimInputEvent e) {
        if (!keyStrokeMode) {
            if (keyMappings.containsKey(e)) {
                e = keyMappings.get(e);
            }
            if(VimInputEvent.ESCAPE.equals(e)) {
                cleanUp();
            } else {
                Token t = TokenFactory.create(e);
                processToken(t);
            }
        } else {
            Token t = KeyStrokeToken.from(vim, e);
            keyStrokeMode = false;
            processToken(t);
        }
        return false;
    }

    public void overrideMapping(VimInputEvent c, VimInputEvent d) {
        if (c.equals(d)) {
            keyMappings.remove(c);
        } else {
            keyMappings.put(c, d);
        }
    }

    void processToken(Token t) {
        vim.getRegisterManager().activateDefaultRegister();
        Platform platform = vim.getPlatform();
        if (t != null) {
            boolean startTokenAssigned = false;
            try {
                if (startToken == null ) {
                    startToken = t;
                    t = null;
                    startTokenAssigned = true;
                    platform.toOperatorPendingMode();
                } else if (t instanceof CommandLineAction
                        || t instanceof SearchModeAction) {
                    ((Action)t).execute(vim);
                    // do NOT clean up - search and command line may return
                    // a Token to be used as argument for startToken
                    return;
                }
                platform.setSpace(startToken.getSpace(t));
                if (startToken.evaluate(vim, t)) {
                    if(startToken.isOperator()) {
                        Token tok = startToken;
                        if (tok instanceof Number) {
                            tok = ((Number)tok).asDefaultRepeater();
                        }
                        CompositeToken change = new CompositeToken(tok, t);
                        vim.getRegisterManager().setLastEdit(change);
                    }
                    platform.beginChange();
                    startToken.getAction().execute(vim);
                    if(vim.inNormalMode()) {
                        afterExecute();
                    }
                    cleanUp();
                } else if (processSelection(t, platform, startTokenAssigned)) {
                    processToken(platform.getSelection());
                }
            } catch (TokenException ex) {
                cleanUp();
            } catch (RuntimeException e) {
                cleanUp();
                throw e;
            }
        }
        platform.setDefaultSpace();
    }

    private boolean processSelection(Token t, Platform platform,
            boolean startTokenAssigned) {
        return vim.inNormalMode() && platform.getSelection() != null &&
        (startTokenAssigned && startToken instanceof AbstractLineAwareToken
                || !startTokenAssigned && t instanceof AbstractLineAwareToken);
    }

    private void cleanUp() {
        startToken = null;
        keyStrokeMode = false;
        // back to normal mode (could be in operator pending mode)
        if (vim.inNormalMode()) {
            vim.getPlatform().toNormalMode();
        }
    }

    class VisualMode implements Mode {

        private int start;
        private final boolean lineWise;

        public VisualMode(boolean lineWise) {
            super();
            this.lineWise = lineWise;
        }

        public boolean type(VimInputEvent e) {
            Platform platform = vim.getPlatform();
            if (keyMappings.containsKey(e) && !keyStrokeMode) {
                e = keyMappings.get(e);
            }
            if (VimInputEvent.ESCAPE.equals(e)) {
                int pos = platform.getPosition();
                if (start < pos) {
                    // in case of forward selection, move 1 char backward
                    pos = Math.max(pos-1, platform.getLineInformation().getBeginOffset());
                }
                platform.setSelection(new Selection(pos, 0));
                vim.toNormalMode();
            } else {
                int pos = platform.getPosition();
                Token t;
                if (keyStrokeMode) {
                    t = KeyStrokeToken.from(vim, e);
                    keyStrokeMode = false;
                } else {
                    t = TokenFactory.create(e);
                }
                if (t instanceof Move || t instanceof KeyStrokeToken) {
                    // when moving forward, the real position is right of
                    // the selection
                    boolean forward = start < pos;
                    if (forward) {
                        // this simulates that the position is on the last char
                        // of the selection
                        platform.setPosition(Math.max(
                                platform.getLineInformation().getBeginOffset(),
                                platform.getPosition()-1));
                    }
                    processToken(t);
                    pos = platform.getPosition();
                    if (forward) {
                        // set position to the right of what we want to have selected
                        pos = Math.min(
                                platform.getLineInformation().getEndOffset(),
                                platform.getPosition()+1);
                    }
                    updateSelection(platform, pos);
                } else {
                    processToken(t);
                    if (platform.getSelection() != null && t instanceof AbstractLineAwareToken) {
                        processToken(platform.getSelection());
                        vim.toNormalMode();
                    }
                }
            }
            return false;
        }

        private void updateSelection(Platform platform, int pos) {
            int end;
            int begin;
            if (lineWise) {
                LineInformation beginLine = platform.getLineInformationOfOffset(start);
                LineInformation endLine = platform.getLineInformationOfOffset(pos);
                if (start < pos) {
                    begin = beginLine.getBeginOffset();
                    end   = endLine.getEndOffset();
                } else {
                    begin = beginLine.getEndOffset();
                    end   = endLine.getBeginOffset();
                }
            } else {
                begin = start;
                end = pos;
                if (begin >= end) {
                    // start has to be included, even on selecting backwards
                    begin += 1;
                }
            }
            platform.setSelection(Selection.fromOffsets(begin, end, lineWise));
        }

        public void initialize() {
            Platform platform = vim.getPlatform();
            start = platform.getPosition();
            int pos = start;
            if (pos < platform.getLineInformation().getEndOffset()) {
                pos += 1;
            }
            updateSelection(platform, pos);
        }

        public void toKeystrokeMode() {
            keyStrokeMode = true;
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
        // end change
        platform.endChange();
        // set horizontal position after change
        if (startToken.isOperator()) {
            vim.updateHorizontalPosition();
        }
    }
}
