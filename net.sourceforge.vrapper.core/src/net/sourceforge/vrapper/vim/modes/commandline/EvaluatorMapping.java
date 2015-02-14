package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.UserCommandCommand;

public class EvaluatorMapping implements Evaluator {

    private final Map<String, Evaluator> actions = new HashMap<String, Evaluator>();
    private final Map<String, String> userDefined = new HashMap<String, String>();
    private final Evaluator defaultCase;

    public EvaluatorMapping () {
        this(null);
    }

    public EvaluatorMapping(Evaluator defaultCase) {
        super();
        this.defaultCase = defaultCase;
    }

    public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
        if (!command.isEmpty()) {
            Evaluator a = actions.get(command.peek());
            if (a != null) {
                command.poll();
                return a.evaluate(vim, command);
            }
        }
        return defaultCase != null ? defaultCase.evaluate(vim, command) : null;
    }

    public void add(String key, Evaluator evaluator) {
        actions.put(key, evaluator);
    }

    /** Adds a {@link Command} to the evaluator list by wrapping it. */
    public void add(String key, Command action) {
        actions.put(key, new CommandWrapper(action));
    }
    
    public void addUserDefined(String key, String command) {
        userDefined.put(key, command);
        add(key, new UserCommandCommand(command));
    }

    Evaluator get(String key) {
        return actions.get(key);
    }
    
    public Map<String, String> getUserDefined() {
        return userDefined;
    }

    public boolean contains(String key) {
        return actions.containsKey(key);
    }

    public Evaluator getDefaultCase() {
        return defaultCase;
    }
    
    /**
     * Check to see if this partial name matches exactly one
     * command name.  If it does, return the full name of that
     * command.  If no match is found, or if multiple matches
     * are found, return null.
     * For example, getNameFromPartial("tabprev") returns "tabprevious"
     * @param partial - String which may or may not be a subset of a command name
     * @return Full command name if exactly one match, otherwise null
     */
    public String getNameFromPartial(String partial) {
    	//chop off trailing "!", it isn't part of the command name
    	if(partial.endsWith("!")) {
    		partial = partial.substring(0, partial.length()-1);
    	}
    	
    	String commandName = null;
    	for(String name : actions.keySet()) {
    		if(name.startsWith(partial)) {
    			if(commandName == null) {
    				//this is our first match
    				//(but keep looping in case there are more)
    				commandName = name;
    			}
    			else {
    				//multiple matches
    				return null;
    			}
    		}
    	}
    	
    	return commandName;
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
