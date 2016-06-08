package net.sourceforge.vrapper.plugin.test.commands;

import java.util.Queue;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import net.sourceforge.vrapper.platform.PlatformSpecificVolatileStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

public class JSSpecificTestCommandsStateProvider implements PlatformSpecificVolatileStateProvider {
    @Override
    public int getVolatilePriority() {
        return 20;
    }
    
    @Override
    public EvaluatorMapping getVolatileCommands() {
        EvaluatorMapping res = new EvaluatorMapping();

        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            if ("js".equals(((FileEditorInput)input).getFile().getFileExtension())) {
                res.add("test-ext-specific", new Evaluator() {
                    @Override
                    public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                        TestUtils.showVimMessage(vim, "Yeah, this command works for JavaScript");
                        return null;
                    }
                });
            }
        }

        return res;
    }

}
