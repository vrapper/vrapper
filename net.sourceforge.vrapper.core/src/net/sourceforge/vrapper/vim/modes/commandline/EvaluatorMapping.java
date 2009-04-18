package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

public class EvaluatorMapping implements Evaluator {

    private final Map<String, Evaluator> actions = new HashMap<String, Evaluator>();

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        if (!command.isEmpty()) {
            Evaluator a = actions.get(command.poll());
            if (a != null) {
                return a.evaluate(vim, command);
            }
        }
        return null;
    }

    public void add(String key, Evaluator evaluator) {
        actions.put(key, evaluator);
    }

    public void add(String key, Command action) {
        actions.put(key, new CommandWrapper(action));
    }

}
