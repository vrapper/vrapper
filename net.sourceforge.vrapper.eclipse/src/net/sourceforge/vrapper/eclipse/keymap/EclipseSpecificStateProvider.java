package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.ChangeTabCommand;
import net.sourceforge.vrapper.eclipse.commands.GoToMarkCommand;
import net.sourceforge.vrapper.eclipse.commands.ListTabsCommand;
import net.sourceforge.vrapper.eclipse.commands.TabNewCommand;
import net.sourceforge.vrapper.eclipse.commands.ToggleFoldingCommand;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;

/**
 * Provides eclipse-specific bindings for command based modes.
 *
 * @author Matthias Radig
 */
@SuppressWarnings("unchecked")
public class EclipseSpecificStateProvider extends AbstractEclipseSpecificStateProvider {

    // Loaded on Eclipse start
    public EclipseSpecificStateProvider() {
        commands.add("eclipseaction", new EclipseActionEvaluator(false, false));
        commands.add("eclipseaction!", new EclipseActionEvaluator(true, false));
        commands.add("eclipseuiaction", new EclipseActionEvaluator(false, true));
        commands.add("eclipseuiaction!", new EclipseActionEvaluator(true, true));

    	commands.add("ls",          dontRepeat(cmd("org.eclipse.ui.window.openEditorDropDown")));
    	commands.add("buffers",     dontRepeat(cmd("org.eclipse.ui.window.openEditorDropDown")));
    	commands.add("maximize",    dontRepeat(cmd("org.eclipse.ui.window.maximizePart")));

    	commands.add("tabnext",     (Command)ChangeTabCommand.NEXT_EDITOR);
    	//have to define this or else 'tabn' is expanded to 'tabnew'
    	commands.add("tabn",        (Command)ChangeTabCommand.NEXT_EDITOR);
    	commands.add("bnext",       (Command)ChangeTabCommand.NEXT_EDITOR);
    	commands.add("tabprevious", (Command)ChangeTabCommand.PREVIOUS_EDITOR);
    	commands.add("bprevious",   (Command)ChangeTabCommand.PREVIOUS_EDITOR);
    	commands.add("tabrewind",   (Command)ChangeTabCommand.FIRST_EDITOR);
    	commands.add("tablast",     (Command)ChangeTabCommand.LAST_EDITOR);

    	// Calls New Wizard dialogue
    	commands.add("tabedit",     (Command)TabNewCommand.NEW_EDITOR);
    	commands.add("tabnew",      (Command)TabNewCommand.NEW_EDITOR);
    	commands.add("tabs",        (Command)ListTabsCommand.INSTANCE);
    }

    @Override
    protected State<Command> visualModeBindings() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;

        return state(
            transitionBind('g',
                    leafBind('U', seq(editText("upperCase"), leaveVisual)),
                    leafBind('u', seq(editText("lowerCase"), leaveVisual))));
    }

    @Override
    protected State<String> visualModeKeymap() {
        return state(leafBind('g', KeyMapResolver.NO_KEYMAP));
    }

    @Override
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = textObjectProvider.textObjects();
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
                leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory"))),
                leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
                leafBind(SpecialKey.TAB, dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
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
            prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), DeselectAllCommand.INSTANCE), textObjects)
         );
        return normalModeBindings;
    }
    
    @Override
    protected State<String> normalModeKeymap() {
        State<String> normalModeKeymap = state(
                leafBind('z', KeyMapResolver.NO_KEYMAP),
                transitionBind('g', 
                        leafBind('t', KeyMapResolver.NO_KEYMAP),
                        leafBind('T', KeyMapResolver.NO_KEYMAP),
                        leafBind('u', KeyMapResolver.OMAP_NAME),
                        leafBind('U', KeyMapResolver.OMAP_NAME)));
        return normalModeKeymap;
    }

    @Override
    protected State<Command> insertModeBindings() {
    	return state(
    		leafCtrlBind('n', dontRepeat(editText("hippieCompletion"))),
    		leafCtrlBind('p', dontRepeat(editText("hippieCompletion")))
    	);
    }
    
    @Override
    protected State<Command> contentAssistModeBindings() {
    	return state(
    		leafCtrlBind('n', dontRepeat(editText("goto.lineDown"))),
    		leafCtrlBind('p', dontRepeat(editText("goto.lineUp")))
        );
    }

}
