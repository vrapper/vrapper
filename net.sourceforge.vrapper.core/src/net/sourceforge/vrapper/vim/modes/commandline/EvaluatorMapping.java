package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

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

    Evaluator get(String key) {
        return actions.get(key);
    }

    public boolean contains(String key) {
        return actions.containsKey(key);
    }

    /** Adds all actions from other EvaluatorMapping.
     * 
     * Current actions *aren't* overridden by new ones.
     */
    public void addAll(EvaluatorMapping other) {
        for (Entry<String, Evaluator> entry: other.actions.entrySet())
            if (!actions.containsKey(entry.getKey()))
                actions.put(entry.getKey(), entry.getValue());
    }

}
