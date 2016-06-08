package net.sourceforge.vrapper.plugin.test.commands;

import java.util.Queue;

import net.sourceforge.vrapper.platform.PlatformSpecificVolatileStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

public class OverridingTestCommandsStateProvider implements PlatformSpecificVolatileStateProvider {
    private boolean regAbolished;
    
    @Override
    public int getVolatilePriority() {
        return 20;
    }
    
    @Override
    public EvaluatorMapping getVolatileCommands() {
        EvaluatorMapping res = new EvaluatorMapping();

        res.add("test-abolish-reg", new Evaluator() {
            @Override
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                regAbolished = true;
                TestUtils.showVimMessage(vim, "OK, now try :reg");
                return null;
            }
        });
        
        res.add("test-return-reg", new Evaluator() {
            @Override
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                regAbolished = false;
                TestUtils.showVimMessage(vim, "You can use it again");
                return null;
            }
        });

        if (regAbolished) {
            res.add("reg", new Evaluator() {
                @Override
                public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                    TestUtils.showVimMessage(vim, "No regs for you");
                    return null;
                }
            });
        }
        
        return res;
    }
}
