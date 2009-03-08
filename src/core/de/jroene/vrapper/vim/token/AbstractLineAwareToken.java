package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

/**
 * A {@link Token} which can act character-wise (like other tokens) or line-wise.
 *
 * @author Matthias Radig
 */
public abstract class AbstractLineAwareToken extends AbstractToken implements Repeatable {

    private int target;
    private Move subject;
    private Token finalToken;
    private Number multiplier;
    private int repeat;

    public AbstractLineAwareToken() {
        super();
    }

    public AbstractLineAwareToken(int target, Move subject, Number multiplier) {
        super();
        this.target = target;
        this.subject = subject;
        this.multiplier = multiplier;
    }

    public abstract Action getAction();

    public boolean isLineDeletion() {
        if(subject != null) {
            return !subject.isHorizontal();
        }
        return true;
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return evaluate0(vim, 1, next, getMultiplier() != null);
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        return evaluate0(vim, times, next, true);
    }

    public boolean evaluate0(VimEmulator vim, int times, Token next,
            boolean useRepeat) throws TokenException {
        repeat = times;
        if (multiplier != null) {
            times *= multiplier.evaluateNumber();
        }
        if (subject != null) {
            return evaluateSubject(vim, times, next, useRepeat);
        }
        if (next == null) {
            return false;
        }
        if (next instanceof WordMove.NextBegin) {
            // this is an exception
            // if a newline follows the word, stop there
            WordMove.NextBegin move = (WordMove.NextBegin)next;
            target = move.getTarget(vim, times, true);
            subject = move;
            return true;
        }
        if (next.getClass().equals(this.getClass())) {
            Platform platform = vim.getPlatform();
            LineInformation line = platform.getLineInformation();
            int lineNumber = line.getNumber() + times - 1;
            target = platform.getLineInformation(lineNumber).getBeginOffset();
            finalToken = next;
            return true;
        }
        if (next instanceof Move) {
            subject = (Move) next;
            return evaluateSubject(vim, times, null, useRepeat);
        }
        if (next instanceof Number) {
            if (multiplier == null) {
                multiplier = (Number)next;
            } else {
                multiplier.evaluate(vim, next);
            }
            return false;
        }
        throw new TokenException();
    }

    private boolean evaluateSubject(VimEmulator vim, int times, Token next, boolean useRepeat)
    throws TokenException {
        boolean result;
        if(useRepeat && subject instanceof RepeatableMove) {
            result = ((RepeatableMove)subject).repeat(vim, times, next);
        } else {
            result = subject.evaluate(vim, next);
        }
        if(result) {
            target = subject.getTarget();
            if (target == -1) {
                throw new TokenException();
            }
            finalToken = next;
        }
        return result;
    }

    int getTarget() {
        return target;
    }

    Move getSubject() {
        return subject;
    }

    Token getFinalToken() {
        return finalToken;
    }

    public Number getMultiplier() {
        return multiplier;
    }

    int getRepeat() {
        return repeat;
    }

    public abstract class LineAwareLineAction implements Action {

        public final void execute(VimEmulator vim) {
            Platform p = vim.getPlatform();
            LineInformation startLine = p.getLineInformation();
            LineInformation targetLine = p.getLineInformationOfOffset(getTarget());
            if (startLine.getNumber() > targetLine.getNumber()) {
                beforeEdit(vim, targetLine, startLine);
                edit(vim, targetLine, startLine);
                afterEdit(vim, targetLine, startLine);
            } else {
                beforeEdit(vim, startLine, targetLine);
                edit(vim, startLine, targetLine);
                afterEdit(vim, startLine, targetLine);
            }
            p.setUndoMark();
        }

        protected abstract void beforeEdit(VimEmulator vim, LineInformation startLine,
                LineInformation targetLine);

        protected abstract void afterEdit(VimEmulator vim, LineInformation startLine, LineInformation endLine);

        private void edit(VimEmulator vim, LineInformation startLine,
                LineInformation targetLine) {
            Platform p = vim.getPlatform();
            int start = startLine.calculateAboveEndOffset(p);
            int end = targetLine.getEndOffset();
            if (start < 0) {
                start = 0;
                int number = targetLine.getNumber();
                if(number < p.getNumberOfLines()-1) {
                    end = p.getLineInformation(number+1).getBeginOffset();
                }
            }
            doEdit(vim, startLine, start, end);
        }

        protected abstract void doEdit(VimEmulator vim, LineInformation originalLine, int start,
                int end);

    }

    public abstract class LineAwareAction implements Action {

        public final void execute(VimEmulator vim) {
            Platform p = vim.getPlatform();
            int start = p.getPosition();
            int end = getTarget();
            if (start < end) {
                if (getSubject().includesTarget()) {
                    end += 1;
                }
                beforeEdit(vim, start, end);
                edit(vim, start, end);
                afterEdit(vim, start, end);
            } else {
                if (getSubject().includesTarget()) {
                    start += 1;
                }
                beforeEdit(vim, end, start);
                edit(vim, end, start);
                afterEdit(vim, end, start);
            }
            p.setUndoMark();
        }

        protected abstract void beforeEdit(VimEmulator vim, int start, int end);
        protected abstract void afterEdit(VimEmulator vim, int start, int end);

        private void edit(VimEmulator vim, int start, int end) {
            Platform p = vim.getPlatform();
            doEdit(vim, p.getPosition(), start, end);
        }

        protected abstract void doEdit(VimEmulator vim, int originalPosition, int start, int end);
    }

    public Space getSpace() {
        return Space.MODEL;
    }

}