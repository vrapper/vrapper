package de.jroene.vrapper.vim.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;

public class EvaluatorMapping implements Evaluator {

    private final Map<String, Evaluator> actions = new HashMap<String, Evaluator>();

    public Object evaluate(VimEmulator vim, Queue<String> command) {
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

    public void add(String key, Action action) {
        actions.put(key, new ActionWrapper(action));
    }

}
