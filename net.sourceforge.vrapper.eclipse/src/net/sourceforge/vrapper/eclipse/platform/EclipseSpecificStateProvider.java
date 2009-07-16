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

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
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

    public class CountedVisualCommand implements Command {

        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            // TODO Auto-generated method stub

        }

        public Command repetition() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        public Command withCount(int count) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public static final EclipseSpecificStateProvider INSTANCE = new EclipseSpecificStateProvider();

    private final HashMap<String, State<Command>> states;
    private final HashMap<String, State<String>> keyMaps;
    private final EvaluatorMapping commands;

    private EclipseSpecificStateProvider() {
       states = new HashMap<String, State<Command>>();
       states.put(NormalMode.NAME, normalModeBindings());
       states.put(VisualMode.NAME, visualModeBindings());
       keyMaps = new HashMap<String, State<String>>();
       keyMaps.put(NormalMode.NAME, normalModeKeymap());
       keyMaps.put(VisualMode.NAME, visualModeKeymap());
       commands = new EvaluatorMapping();
       Command formatAll = javaEditText("format");
       commands.add("eclipseaction", new EclipseActionEvaluator(false));
       commands.add("eclipseaction!", new EclipseActionEvaluator(true));
       commands.add("formatall", formatAll);
       commands.add("format", formatAll);
       commands.add("fm", formatAll);
    }

    private State<Command> visualModeBindings() {
        Command leaveVisual = new LeaveVisualModeCommand();
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

    private State<String> normalModeKeymap() {
        State<String> normalModeKeymap = state(
                        leafBind('z', KeyMapResolver.NO_KEYMAP),
                        leafBind('g', KeyMapResolver.NO_KEYMAP));
        return normalModeKeymap;
    }

    private State<String> visualModeKeymap() {
        return state(leafBind('g', KeyMapResolver.NO_KEYMAP));
    }

    private State<Command> normalModeBindings() {
        Command deselectAll = new CountIgnoringNonRepeatableCommand() {
            public void execute(EditorAdaptor editorMode) {
                editorMode.setPosition(editorMode.getSelection().getEnd(), true);
            }
        };
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
                        leafBind('r', javaEditText("refactor.quickMenu")),
                        leafBind('R', javaEditText("rename.element")),
                        leafBind('t', cmd("org.eclipse.ui.window.nextEditor")),
                        leafBind('T', cmd("org.eclipse.ui.window.previousEditor"))),
                leafCtrlBind('f', go("goto.pageDown")),
                leafCtrlBind('y', dontRepeat(editText("scroll.lineUp"))),
                leafCtrlBind('e', dontRepeat(editText("scroll.lineDown"))),
                leafCtrlBind(']', seq(javaEditText("open.editor"), deselectAll)), // NOTE: deselect won't work in other editor
                leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
                leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory")))),
            prefixedOperatorCmds('g', 'c', seq(javaEditText("toggle.comment"), deselectAll), textObjects),
            prefixedOperatorCmds('g', 'u', seq(editText("lowerCase"), deselectAll), textObjects),
            prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), deselectAll), textObjects),
            operatorCmds('=', seq(javaEditText("indent"), deselectAll), textObjects),
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

    private static Command go(String where) {
        return new EclipseCommand("org.eclipse.ui.edit.text.goto." + where);
    }


    private static Command cmd(String command) {
        return new EclipseCommand(command);
    }

//    private static Command edit(String command) {
//        return new EclipseCommand("org.eclipse.ui.edit." + command);
//    }

    private static EclipseCommand editText(String command) {
        return new EclipseCommand("org.eclipse.ui.edit.text." + command);
    }

    private static Command javaEditText(String cmd) {
        // FIXME: this is temporary, keymap should be language-independent
        return new EclipseCommand("org.eclipse.jdt.ui.edit.text.java." + cmd);
    }

    private static class EclipseActionEvaluator implements Evaluator {

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

}
