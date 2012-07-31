package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.ChangeTabCommand;
import net.sourceforge.vrapper.eclipse.commands.EclipseShiftOperation;
import net.sourceforge.vrapper.eclipse.commands.EclipseVisualMotionCommand;
import net.sourceforge.vrapper.eclipse.commands.ToggleFoldingCommand;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperationCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Provides eclipse-specific bindings for command based modes.
 *
 * @author Matthias Radig
 */
@SuppressWarnings("unchecked")
public class EclipseSpecificStateProvider extends AbstractEclipseSpecificStateProvider {

    public EclipseSpecificStateProvider() {
        commands.add("eclipseaction", new EclipseActionEvaluator(false));
        commands.add("eclipseaction!", new EclipseActionEvaluator(true));
    }

    @Override
    protected State<Command> visualModeBindings() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        Command shiftRight = new SelectionBasedTextOperationCommand(EclipseShiftOperation.Visual.RIGHT);
        Command shiftLeft = new SelectionBasedTextOperationCommand(EclipseShiftOperation.Visual.LEFT);
        Command pageUp = new EclipseVisualMotionCommand("org.eclipse.ui.edit.text.goto.pageUp");
        Command pageDown = new EclipseVisualMotionCommand("org.eclipse.ui.edit.text.goto.pageDown");

        return state(
            transitionBind('g',
                    leafBind('U', seq(editText("upperCase"),      leaveVisual)),
                    leafBind('u', seq(editText("lowerCase"),      leaveVisual))),
            leafBind(SpecialKey.PAGE_DOWN, pageDown),
            leafBind(SpecialKey.PAGE_UP, pageUp),
            leafCtrlBind('f', pageDown),
            leafCtrlBind('b', pageUp),
            leafCtrlBind('d', pageDown),
            leafCtrlBind('u', pageUp),
            leafBind('>', shiftRight),
            leafBind('<', shiftLeft));
    }

    @Override
    protected State<String> normalModeKeymap() {
        State<String> normalModeKeymap = state(
                        leafBind('z', KeyMapResolver.NO_KEYMAP),
                        leafBind('g', KeyMapResolver.NO_KEYMAP));
        return normalModeKeymap;
    }

    @Override
    protected State<String> visualModeKeymap() {
        return state(leafBind('g', KeyMapResolver.NO_KEYMAP));
    }

    @Override
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        State<Command> normalModeBindings = StateUtils.union(
            state(
                transitionBind('z',
                        leafBind('a', ToggleFoldingCommand.INSTANCE),
                        leafBind('o', dontRepeat(editText("folding.expand"))),
                        leafBind('R', dontRepeat(editText("folding.expand_all"))),
                        leafBind('c', dontRepeat(editText("folding.collapse"))),
                        leafBind('M', dontRepeat(editText("folding.collapse_all")))),
                transitionBind('g',
                        leafBind('t', (Command)ChangeTabCommand.NEXT_EDITOR),
                        leafBind('T', (Command)ChangeTabCommand.PREVIOUS_EDITOR)),
                leafCtrlBind('f', dontRepeat(go("pageDown"))),
                leafCtrlBind('b', dontRepeat(go("pageUp"))),
                leafCtrlBind('d', dontRepeat(go("pageDown"))),
                leafCtrlBind('u', dontRepeat(go("pageUp"))),
                leafBind(SpecialKey.PAGE_DOWN, dontRepeat(go("pageDown"))),
                leafBind(SpecialKey.PAGE_UP, dontRepeat(go("pageUp"))),
                leafCtrlBind('y', dontRepeat(editText("scroll.lineUp"))),
                leafCtrlBind('e', dontRepeat(editText("scroll.lineDown"))),
                leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
                leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory")))),
            prefixedOperatorCmds('g', 'u', seq(editText("lowerCase"), DeselectAllCommand.INSTANCE), textObjects),
            prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), DeselectAllCommand.INSTANCE), textObjects),
            operatorCmds('>', EclipseShiftOperation.Normal.RIGHT, textObjects),
            operatorCmds('<', EclipseShiftOperation.Normal.LEFT, textObjects)
         );
        return normalModeBindings;
    }
    
    @Override
    protected State<Command> insertModeBindings() {
    	return state(
    		leafCtrlBind('n', dontRepeat(editText("hippieCompletion"))),
    		leafCtrlBind('p', dontRepeat(editText("hippieCompletion")))
    	);
    }

}
