package net.sourceforge.vrapper.plugin.clangformat.provider;

import java.util.Queue;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.clangformat.commands.ClangFormat;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public class ClangFormatProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new ClangFormatProvider();

    protected static class ClangFormatEvaluator implements Evaluator {

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            try {
                new ClangFormat(command).execute(vim);
            } catch (Exception e) {
                vim.getUserInterfaceService().setErrorMessage(e.getMessage());
            }
            return null;
        }
    }

    protected static class ClangFormatStyleEvaluator implements Evaluator {
        private final boolean isLocal;

        ClangFormatStyleEvaluator(final boolean isLocal)
        {
            this.isLocal = isLocal;
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {

            final String style = StringUtils.join(" ", command).replaceAll("(?i)<SPACE>", " ");
            if (isLocal) {
                vim.getConfiguration().setLocal(ClangFormat.STYLE_OPT, style);
            } else {
                vim.getConfiguration().set(ClangFormat.STYLE_OPT, style);
            }
            return null;
        }
    }

    public ClangFormatProvider() {
        name = "ClangFormat State Provider";
        commands.add("clang-format", new ClangFormatEvaluator());
        commands.add("clang-format-default-style", new ClangFormatStyleEvaluator(false));
        commands.add("clang-format-style", new ClangFormatStyleEvaluator(true));
    }

}
