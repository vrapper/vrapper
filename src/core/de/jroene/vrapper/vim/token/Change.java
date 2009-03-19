package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.InsertMode;
import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.Action;

/**
 * Like {@link Delete}, but switches to insert mode after the deletion.
 *
 * @author Matthias Radig
 */
public class Change extends Delete {

    @Override
    public Action getAction() {
        return isLineDeletion() ? new LineChangeAction() : new ChangeAction();
    }

    @Override
    public boolean evaluate0(VimEmulator vim, int times, Token next, boolean useRepeat)
    throws TokenException {
        // cw behaves like ce
        if (next instanceof WordMove.NextBegin) {
            next = ((WordMove.NextBegin)next).createNextEndMove();
        }
        return super.evaluate0(vim, times, next, useRepeat);
    }

    private Token createDelete() {
        Token finalToken = getFinalToken();
        int times = getRepeat() * (getMultiplier() != null ? getMultiplier().evaluateNumber() : 1);
        Number multiplier = new Number(String.valueOf(times));
        if(finalToken != null && finalToken.getClass().equals(Change.class)) {
            Token delete = new Delete.BufferNeutral(0, null, multiplier);
            return new CompositeToken(delete, delete);
        }
        Move t = new CompositeToken(getSubject(), finalToken);
        return new Delete.BufferNeutral(0, t, multiplier);
    }

    public class ChangeAction extends DeleteAction {

        @Override
        protected void afterEdit(VimEmulator vim, int start, int end) {
            super.afterEdit(vim, start, end);
            Token delete = createDelete();
            vim.toInsertMode(new InsertMode.Parameters(false, true, 1, start, delete));
        }

    }

    /**
     * This Token deletes lines and the switches to insert mode. If the
     * "autoindent" option is set, the preceeding whitespace of the first line
     * is preserved.
     *
     * @author Matthias Radig
     */
    public class LineChangeAction extends LineDeleteAction {

        private String indent;

        @Override
        protected void afterEdit(VimEmulator vim, LineInformation startLine, LineInformation endLine) {
            super.afterEdit(vim, startLine, endLine);
            Platform p = vim.getPlatform();
            int position = p.getPosition();
            p.replace(position, 0, indent+vim.getVariables().getNewLine());
            p.setPosition(position+indent.length());
            vim.getPlatform().setRepaint(true);
            Token delete = createDelete();
            vim.toInsertMode(new InsertMode.Parameters(true, true, 1, startLine.getBeginOffset(), delete));
        }

        @Override
        protected void beforeEdit(VimEmulator vim, LineInformation startLine,
                LineInformation endLine) {
            super.beforeEdit(vim, startLine, endLine);
            vim.getPlatform().setRepaint(false);
            indent = vim.getVariables().isAutoIndent() ? VimUtils.getIndent(vim, startLine) : "";
        }
    }
}
