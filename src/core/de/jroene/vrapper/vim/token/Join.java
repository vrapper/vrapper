package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Space;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.Action;

/**
 * Joins a number of lines.
 *
 * @author Matthias Radig
 */
public class Join extends AbstractToken implements Repeatable {

    private int joins;

    public Action getAction() {
        return new JoinAction();
    }

    public boolean evaluate(VimEmulator vim, Token next) throws TokenException {
        return repeat(vim, 2, next);
    }

    public boolean repeat(VimEmulator vim, int times, Token next)
    throws TokenException {
        if (times == 1) {
            times = 2;
        }
        Platform p = vim.getPlatform();
        LineInformation currentLine = p.getLineInformation();
        if (p.getNumberOfLines() < currentLine.getNumber()+times) {
            throw new TokenException();
        }
        joins = times - 1;
        return true;
    }

    public Space getSpace() {
        return Space.MODEL;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    public class JoinAction implements Action {

        public void execute(VimEmulator vim) {
            for(int i = 0; i < joins; i++) {
                join(vim);
            }
            vim.getPlatform().setUndoMark();
        }

        private void join(VimEmulator vim) {
            Platform p = vim.getPlatform();
            LineInformation line = p.getLineInformation();
            StringBuilder sb = new StringBuilder();
            if(line.getLength() > 1 &&
                    !VimUtils.isWhiteSpace(p.getText(line.getEndOffset()-1, 1))) {
                sb.append(VimConstants.SPACE);
            }

            LineInformation other = p.getLineInformation(line.getNumber()+1);
            String content = VimUtils.getWithoutIndent(vim, other);
            sb.append(content);
            if(sb.length() == 0 ||
                    !VimUtils.isNewLine(sb.substring(sb.length()-1, sb.length()))) {
                sb.append(vim.getVariables().getNewLine().nl);
            }
            p.replace(line.getEndOffset(),
                    other.getEndOffset()-line.getEndOffset()+1,
                    sb.toString());
        }

    }

}
