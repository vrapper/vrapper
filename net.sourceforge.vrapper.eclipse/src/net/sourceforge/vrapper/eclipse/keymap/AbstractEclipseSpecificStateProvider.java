package net.sourceforge.vrapper.eclipse.keymap;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ContentAssistMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public abstract class AbstractEclipseSpecificStateProvider implements
        PlatformSpecificStateProvider, Comparable<AbstractEclipseSpecificStateProvider> {

    protected HashMap<String, State<Command>> states = null;
    protected HashMap<String, State<String>> keyMaps = null;
    protected State<TextObject> textObjects = null;
    protected final EvaluatorMapping commands = new EvaluatorMapping();
    protected int priority = 1;
    protected String name;

    protected AbstractEclipseSpecificStateProvider() {
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

    protected State<Command> normalModeBindings(State<TextObject> textObjects) {
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
    
    protected State<Command> contentAssistModeBindings() {
        return EmptyState.getInstance();
    }
    
    protected State<TextObject> textObjects() {
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

        private boolean force;
        private boolean async;

        protected EclipseActionEvaluator(boolean force, boolean async) {
            super();
            this.force = force;
            this.async = async;
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            String name = command.poll();
            if("!".equals(name)) {
            	//we made a change where the '!' is separated from the command name
            	//if that's the case (eclipseaction!), this isn't the name yet
            	force = true;
            	name = command.poll();
            }
            String action = command.poll();
            if (name != null && action != null) {
                CommandLineMode mode = (CommandLineMode) vim
                        .getMode(CommandLineMode.NAME);
                mode.addCommand(name, new EclipseCommand(action, async), force);
            }
            return null;
        }

    }

    public String getFileType() {
        return "text";
    }

    public State<Command> getState(String modeName) {
        assert states != null;
        return states.get(modeName);
    }

    protected final Map<String, State<Command>> getStates(State<TextObject> textObjects) {
        if (states == null) {
            states = new HashMap<String, State<Command>>();
            states.put(NormalMode.NAME, normalModeBindings(textObjects));
            states.put(AbstractVisualMode.NAME, visualModeBindings());
            states.put(InsertMode.NAME, insertModeBindings());
            states.put(ContentAssistMode.NAME, contentAssistModeBindings());
        }
        return states;
    }

    public State<String> getKeyMaps(String name) {
        assert keyMaps != null;
        return keyMaps.get(name);
    }

    protected final Map<String, State<String>> getKeyMaps() {
        if (keyMaps == null) {
            keyMaps = new HashMap<String, State<String>>();
            keyMaps.put(NormalMode.NAME, normalModeKeymap());
            keyMaps.put(AbstractVisualMode.NAME, visualModeKeymap());
        }
        return keyMaps;
    }

    public final EvaluatorMapping getCommands() {
        return commands;
    }

    public final State<TextObject> getTextObjects() {
        if (textObjects == null) {
            textObjects = textObjects();
        }
        return textObjects;
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