package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ComplexLocalOptionEvaluator extends ComplexOptionEvaluator {

    @Override
    protected <T> void set(EditorAdaptor adaptor, Option<T> opt, T value) throws ValueException {
        adaptor.getConfiguration().setLocal(opt, value);
    }

}
