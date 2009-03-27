package de.jroene.vrapper.vim.commandline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.token.Token;

public class EvaluatorMapping implements Evaluator {

    private final Map<String, Evaluator> actions = new HashMap<String, Evaluator>();

    public Token evaluate(VimEmulator vim, Iterator<String> command) {
        if (command.hasNext()) {
            Evaluator a = actions.get(command.next());
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
