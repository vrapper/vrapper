package net.sourceforge.vrapper.plugin.test.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificVolatileStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

public class HigherPriorityTestCommandsStateProvider implements PlatformSpecificVolatileStateProvider {
    @Override
    public int getVolatilePriority() {
        return 100;
    }
    
    @Override
    public EvaluatorMapping getVolatileCommands() {
        EvaluatorMapping res = new EvaluatorMapping();
        res.add("test-volatile-priority", new Evaluator() {
            @Override
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                TestUtils.showVimMessage(vim, "I am of somewhat higher priority than LPTCSP, but still not high enough");
                return null;
            }
        });
        return res;
    }

    @Override
    public Map<String, State<KeyMapInfo>> getVolatileKeyMaps() {
        return new HashMap<String, State<KeyMapInfo>>();
    }
}
