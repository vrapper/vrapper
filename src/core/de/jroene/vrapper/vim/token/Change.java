package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimConstants;
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

    public class ChangeAction extends DeleteAction {

        @Override
        protected void afterEdit(VimEmulator vim, int start, int end) {
            super.afterEdit(vim, start, end);
            vim.toInsertMode();
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
            p.replace(position, 0, indent+VimConstants.NEWLINE, false);
            p.setPosition(position+indent.length());
            vim.toInsertMode();
        }

        @Override
        protected void beforeEdit(VimEmulator vim, LineInformation startLine,
                LineInformation endLine) {
            super.beforeEdit(vim, startLine, endLine);
            indent = vim.getVariables().isAutoIndent() ? VimUtils.getIndent(vim, startLine) : "";
        }
    }
}
