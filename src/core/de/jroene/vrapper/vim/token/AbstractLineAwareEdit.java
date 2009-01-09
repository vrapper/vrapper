package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.register.RegisterContent;

/**
 * Does the same as {@link AbstractLineAwareToken} but also copies the text
 * of its "area of effect" into the currently active buffer.
 *
 * @author Matthias Radig
 */
public abstract class AbstractLineAwareEdit extends AbstractLineAwareToken {

    public AbstractLineAwareEdit() {
        super();
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    public abstract class LineEditAction extends LineAwareLineAction implements Action {

        @Override
        protected void beforeEdit(VimEmulator vim, LineInformation startLine, LineInformation endLine) {
            int start = startLine.getBeginOffset();
            int end = endLine.getEndOffset();
            String text = vim.getPlatform().getText(start, end-start);
            RegisterContent content = new RegisterContent(true, text);
            vim.getRegisterManager().getActiveRegister().setContent(content);
        }

        @Override
        protected void afterEdit(VimEmulator vim, LineInformation startLine,
                LineInformation endLine) {
            // do nothing
        }
    }

    public abstract class EditAction extends LineAwareAction implements Action {

        @Override
        protected void beforeEdit(VimEmulator vim, int start, int end) {
            String text = vim.getPlatform().getText(start, end-start);
            RegisterContent content = new RegisterContent(false, text);
            vim.getRegisterManager().getActiveRegister().setContent(content);
        }

        @Override
        protected void afterEdit(VimEmulator vim, int start, int end) {
            // do nothing
        }
    }

}