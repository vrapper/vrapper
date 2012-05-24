package net.sourceforge.vrapper.eclipse.keymap;

import java.util.HashMap;
import java.util.Queue;

import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public abstract class AbstractEclipseSpecificStateProvider implements
        PlatformSpecificStateProvider, Comparable<AbstractEclipseSpecificStateProvider> {

    protected final HashMap<String, State<Command>> states = new HashMap<String, State<Command>>();
    protected final HashMap<String, State<String>> keyMaps = new HashMap<String, State<String>>();
    protected final EvaluatorMapping commands = new EvaluatorMapping();
    protected int priority = 1;
    protected String name;

    protected AbstractEclipseSpecificStateProvider() {
        states.put(NormalMode.NAME, normalModeBindings());
        states.put(AbstractVisualMode.NAME, visualModeBindings());
        keyMaps.put(NormalMode.NAME, normalModeKeymap());
        keyMaps.put(AbstractVisualMode.NAME, visualModeKeymap());
        states.put(InsertMode.NAME, insertModeBindings());
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        try {
            String stringValue = config.getAttribute("priority");
            name = config.getAttribute("name");
            if (stringValue != null)
                priority = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            VrapperLog.error("wrong format of priority", e);
        }
    }

    protected State<Command> normalModeBindings() {
        return EmptyState.getInstance();
    }

    protected State<String> normalModeKeymap() {
        return EmptyState.getInstance();
    }

    protected State<Command> visualModeBindings() {
        return EmptyState.getInstance();
    }

    protected State<String> visualModeKeymap() {
        return EmptyState.getInstance();
    }
    
    protected State<Command> insertModeBindings() {
        return EmptyState.getInstance();
    }
    

    protected static Command go(String where) {
        return new EclipseCommand("org.eclipse.ui.edit.text.goto." + where);
    }

    protected static Command cmd(String command) {
        return new EclipseCommand(command);
    }

    protected static EclipseCommand editText(String command) {
        return new EclipseCommand("org.eclipse.ui.edit.text." + command);
    }
    
    public String getName() {
        return name;
    }

    protected static class EclipseActionEvaluator implements Evaluator {

        private final boolean force;

        protected EclipseActionEvaluator(boolean force) {
            super();
            this.force = force;
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            String name = command.poll();
            String action = command.poll();
            if (name != null && action != null) {
                CommandLineMode mode = (CommandLineMode) vim
                        .getMode(CommandLineMode.NAME);
                mode.addCommand(name, new EclipseCommand(action), force);
            }
            return null;
        }

    }

    public String getFileType() {
        return "text";
    }

    public State<Command> getState(String modeName) {
        return states.get(modeName);
    }

    public State<String> getKeyMaps(String name) {
        return keyMaps.get(name);
    }

    public EvaluatorMapping getCommands() {
        return commands;
    }

    protected void addFormatCommands(Command formatAll) {
        if (formatAll != null) {
            commands.add("formatall", formatAll);
            commands.add("format", formatAll);
            commands.add("fmt", formatAll);
            commands.add("fm", formatAll);
        }
    }

    public int compareTo(AbstractEclipseSpecificStateProvider o) {
        return -Integer.valueOf(priority).compareTo(Integer.valueOf(o.priority));
    }

}