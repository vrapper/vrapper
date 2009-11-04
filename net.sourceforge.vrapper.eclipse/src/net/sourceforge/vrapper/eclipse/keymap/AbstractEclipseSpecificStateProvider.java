package net.sourceforge.vrapper.eclipse.keymap;

import java.util.HashMap;
import java.util.Queue;

import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public abstract class AbstractEclipseSpecificStateProvider implements
        PlatformSpecificStateProvider {

    protected final HashMap<String, State<Command>> states = new HashMap<String, State<Command>>();
    protected final HashMap<String, State<String>> keyMaps = new HashMap<String, State<String>>();
    protected final EvaluatorMapping commands = new EvaluatorMapping();

    protected AbstractEclipseSpecificStateProvider() {
        states.put(NormalMode.NAME, normalModeBindings());
        states.put(VisualMode.NAME, visualModeBindings());
        keyMaps.put(NormalMode.NAME, normalModeKeymap());
        keyMaps.put(VisualMode.NAME, visualModeKeymap());
    }

    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException { /* NOP */
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
        return null;
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

}