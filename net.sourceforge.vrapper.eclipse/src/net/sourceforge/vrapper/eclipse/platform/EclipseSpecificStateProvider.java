package net.sourceforge.vrapper.eclipse.platform;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;

import java.util.HashMap;
import java.util.Queue;

import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

/**
 * Provides eclipse-specific bindings for command based modes.
 *
 * @author Matthias Radig
 */
@SuppressWarnings("unchecked")
public class EclipseSpecificStateProvider implements
        PlatformSpecificStateProvider {

    public static final EclipseSpecificStateProvider INSTANCE = new EclipseSpecificStateProvider();

    protected final HashMap<String, State<Command>> states;
    protected final HashMap<String, State<String>> keyMaps;
    protected final EvaluatorMapping commands;

    protected EclipseSpecificStateProvider() {
       states = new HashMap<String, State<Command>>();
       states.put(NormalMode.NAME, normalModeBindings());
       states.put(VisualMode.NAME, visualModeBindings());
       keyMaps = new HashMap<String, State<String>>();
       keyMaps.put(NormalMode.NAME, normalModeKeymap());
       keyMaps.put(VisualMode.NAME, visualModeKeymap());
       commands = new EvaluatorMapping();
       commands.add("eclipseaction", new EclipseActionEvaluator(false));
       commands.add("eclipseaction!", new EclipseActionEvaluator(true));
        Command formatAll = getFormatCommand();
        if (formatAll != null) {
            commands.add("formatall", formatAll);
            commands.add("format", formatAll);
            commands.add("fmt", formatAll);
            commands.add("fm", formatAll);
        }
    }

    protected Command getFormatCommand() {
        return null;
    }

    protected State<Command> visualModeBindings() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        Command shiftRight = new EclipseShiftOperation.Visual(false);
        Command shiftLeft = new EclipseShiftOperation.Visual(true);
        return state(
            transitionBind('g',
                    leafBind('c', seq(editText("toggle.comment"), leaveVisual)),
                    leafBind('U', seq(editText("upperCase"),      leaveVisual)),
                    leafBind('u', seq(editText("lowerCase"),      leaveVisual))),
            leafBind('>', shiftRight),
            leafBind('<', shiftLeft));
    }

    protected State<String> normalModeKeymap() {
        State<String> normalModeKeymap = state(
                        leafBind('z', KeyMapResolver.NO_KEYMAP),
                        leafBind('g', KeyMapResolver.NO_KEYMAP));
        return normalModeKeymap;
    }

    protected State<String> visualModeKeymap() {
        return state(leafBind('g', KeyMapResolver.NO_KEYMAP));
    }

    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        State<Command> normalModeBindings = StateUtils.union(
            state(
                leafBind('J', (Command) editText("join.lines")),
                transitionBind('z',
                        leafBind('o', dontRepeat(editText("folding.expand"))),
                        leafBind('R', dontRepeat(editText("folding.expand_all"))),
                        leafBind('c', dontRepeat(editText("folding.collapse"))),
                        leafBind('M', dontRepeat(editText("folding.collapse_all")))),
                transitionBind('g',
                        leafBind('t', cmd("org.eclipse.ui.window.nextEditor")),
                        leafBind('T', cmd("org.eclipse.ui.window.previousEditor"))),
                leafCtrlBind('f', go("pageDown")),
                leafCtrlBind('b', go("pageUp")),
                leafBind(SpecialKey.PAGE_DOWN, go("pageDown")),
                leafBind(SpecialKey.PAGE_UP, go("pageUp")),
                leafCtrlBind('y', dontRepeat(editText("scroll.lineUp"))),
                leafCtrlBind('e', dontRepeat(editText("scroll.lineDown"))),
                leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
                leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory")))),
            prefixedOperatorCmds('g', 'u', seq(editText("lowerCase"), DeselectAllCommand.INSTANCE), textObjects),
            prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), DeselectAllCommand.INSTANCE), textObjects),
            operatorCmds('>', new EclipseShiftOperation.Normal(false), textObjects),
            operatorCmds('<', new EclipseShiftOperation.Normal(true), textObjects)
         );
        return normalModeBindings;
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

//    private static Motion javaGoTo(String where, BorderPolicy borderPolicy) {
//        // FIXME: this is temporary, keymap should be language-independent
//        return new EclipseMoveCommand("org.eclipse.jdt.ui.edit.text.java.goto." + where, borderPolicy);
//    }


//    private static Motion go(String where, BorderPolicy borderPolicy) {
//        return new EclipseMoveCommand("org.eclipse.ui.edit.text.goto." + where, borderPolicy);
//    }

    protected static Command go(String where) {
        return new EclipseCommand("org.eclipse.ui.edit.text.goto." + where);
    }


    protected static Command cmd(String command) {
        return new EclipseCommand(command);
    }

//    private static Command edit(String command) {
//        return new EclipseCommand("org.eclipse.ui.edit." + command);
//    }

    protected static EclipseCommand editText(String command) {
        return new EclipseCommand("org.eclipse.ui.edit.text." + command);
    }

    protected static class EclipseActionEvaluator implements Evaluator {

        private final boolean force;

        private EclipseActionEvaluator(boolean force) {
            super();
            this.force = force;
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            String name = command.poll();
            String action = command.poll();
            if (name != null && action != null) {
                CommandLineMode mode = (CommandLineMode) vim.getMode(CommandLineMode.NAME);
                mode.addCommand(name, new EclipseCommand(action), force);
            }
            return null;
        }

    }

    public String getFileType() {
        return "text";
    }

}
