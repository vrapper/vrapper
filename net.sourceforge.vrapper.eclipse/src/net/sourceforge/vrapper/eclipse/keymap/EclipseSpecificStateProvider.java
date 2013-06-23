package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
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
import net.sourceforge.vrapper.eclipse.commands.GoToMarkCommand;
import net.sourceforge.vrapper.eclipse.commands.TabNewCommand;
import net.sourceforge.vrapper.eclipse.commands.ToggleFoldingCommand;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.vim.VimConstants;
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

    // Loaded on Eclipse start
    public EclipseSpecificStateProvider() {
        commands.add("eclipseaction", new EclipseActionEvaluator(false));
        commands.add("eclipseaction!", new EclipseActionEvaluator(true));

    	commands.add("ls",          dontRepeat(cmd("org.eclipse.ui.window.openEditorDropDown")));
    	commands.add("buffers",     dontRepeat(cmd("org.eclipse.ui.window.openEditorDropDown")));
    	commands.add("maximize",    dontRepeat(cmd("org.eclipse.ui.window.maximizePart")));

    	commands.add("tabnext",     (Command)ChangeTabCommand.NEXT_EDITOR);
    	//have to define this or else 'tabn' is expanded to 'tabnew'
    	commands.add("tabn",        (Command)ChangeTabCommand.NEXT_EDITOR);
    	commands.add("bnext",       (Command)ChangeTabCommand.NEXT_EDITOR);
    	commands.add("tabprevious", (Command)ChangeTabCommand.PREVIOUS_EDITOR);
    	commands.add("bprevious",   (Command)ChangeTabCommand.PREVIOUS_EDITOR);
    	commands.add("tabrewind",     (Command)ChangeTabCommand.FIRST_EDITOR);
    	commands.add("tablast",     (Command)ChangeTabCommand.LAST_EDITOR);

    	// Calls New Wizard dialogue
    	commands.add("tabedit",     (Command)TabNewCommand.NEW_EDITOR);
    	commands.add("tabnew",      (Command)TabNewCommand.NEW_EDITOR);
    }

    @Override
    protected State<Command> visualModeBindings() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        Command shiftRight = new SelectionBasedTextOperationCommand(EclipseShiftOperation.Visual.RIGHT);
        Command shiftLeft = new SelectionBasedTextOperationCommand(EclipseShiftOperation.Visual.LEFT);

        return state(
            transitionBind('g',
                    leafBind('U', seq(editText("upperCase"),      leaveVisual)),
                    leafBind('u', seq(editText("lowerCase"),      leaveVisual))),
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
                leafCtrlBind('y', dontRepeat(editText("scroll.lineUp"))),
                leafCtrlBind('e', dontRepeat(editText("scroll.lineDown"))),
                leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
                leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory"))),
                transitionBind('\\', convertKeyStroke(
                        GoToMarkCommand.EDITOR_CONVERTER,
                        VimConstants.PRINTABLE_KEYSTROKES)),
                transitionBind('\'', convertKeyStroke(
                        GoToMarkCommand.LINEWISE_CONVERTER,
                        VimConstants.PRINTABLE_KEYSTROKES)),
                transitionBind('`', convertKeyStroke(
                        GoToMarkCommand.CHARWISE_CONVERTER,
                        VimConstants.PRINTABLE_KEYSTROKES))),
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
