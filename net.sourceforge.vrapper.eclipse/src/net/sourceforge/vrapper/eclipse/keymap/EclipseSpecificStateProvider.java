package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorKeyMap;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.seq;

import java.util.EnumSet;

import net.sourceforge.vrapper.eclipse.commands.ChangeTabCommand;
import net.sourceforge.vrapper.eclipse.commands.EclipseMotionPlugState;
import net.sourceforge.vrapper.eclipse.commands.EclipsePlugState;
import net.sourceforge.vrapper.eclipse.commands.GoToMarkCommand;
import net.sourceforge.vrapper.eclipse.commands.ListTabsCommand;
import net.sourceforge.vrapper.eclipse.commands.TabNewCommand;
import net.sourceforge.vrapper.eclipse.commands.ToggleFoldingCommand;
import net.sourceforge.vrapper.eclipse.modes.InsertExpandMode;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.KeyStroke.Modifier;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.StateUtils;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.InsertMode;

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

        commands.add("files",       dontRepeat(cmd("org.eclipse.ui.window.openEditorDropDown")));
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

        return union(
          state(
            leafBind('u', seq(editText("lowerCase"), leaveVisual)),
            leafBind('U', seq(editText("upperCase"), leaveVisual)),
            leafBind(new SimpleKeyStroke('c', EnumSet.of(Modifier.COMMAND)),
                    seq(dontRepeat(cmd("org.eclipse.ui.edit.copy")), leaveVisual)),
            leafBind(new SimpleKeyStroke('x', EnumSet.of(Modifier.COMMAND)),
                    seq(dontRepeat(cmd("org.eclipse.ui.edit.cut")), leaveVisual)),
            leafBind(new SimpleKeyStroke('v', EnumSet.of(Modifier.COMMAND)),
                    seq(dontRepeat(cmd("org.eclipse.ui.edit.paste")), leaveVisual)),
            transitionBind('g',
                    leafBind('U', seq(editText("upperCase"), leaveVisual)),
                    leafBind('u', seq(editText("lowerCase"), leaveVisual)))),
          new VisualMotionState(EclipseMotionPlugState.INSTANCE),
          EclipsePlugState.VISUAL_INSTANCE);
    }

    @Override
    protected State<KeyMapInfo> visualModeKeymap() {
        return EmptyState.getInstance();
    }

    @Override
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = textObjectProvider.textObjects();
        State<Command> normalModeBindings = StateUtils.union(
            new GoThereState(EclipseMotionPlugState.INSTANCE),
            EclipsePlugState.INSTANCE,
            state(
                transitionBind('z',
                        leafBind('a', ToggleFoldingCommand.DEFAULTINSTANCE),
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
                        VimConstants.PRINTABLE_KEYSTROKES)),
                // These might be disabled when there is no selection
                leafBind(new SimpleKeyStroke('c', EnumSet.of(Modifier.COMMAND)),
                        dontRepeat(cmd("org.eclipse.ui.edit.copy"))),
                leafBind(new SimpleKeyStroke('x', EnumSet.of(Modifier.COMMAND)),
                        dontRepeat(cmd("org.eclipse.ui.edit.cut"))),
                leafBind(new SimpleKeyStroke('v', EnumSet.of(Modifier.COMMAND)),
                        dontRepeat(cmd("org.eclipse.ui.edit.paste")))),
            prefixedOperatorCmds('g', 'u', seq(editText("lowerCase"), DeselectAllCommand.INSTANCE), textObjects),
            prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), DeselectAllCommand.INSTANCE), textObjects)
         );
        return normalModeBindings;
    }
    
    @Override
    protected State<KeyMapInfo> normalModeKeymap() {
        State<KeyMapInfo> normalModeKeymap = state(
                transitionBind('g', 
                        operatorKeyMap('u'),
                        operatorKeyMap('U')));
        return normalModeKeymap;
    }

    @Override
    protected State<Command> insertModeBindings() {
        return StateUtils.union(
                EclipsePlugState.INSTANCE,
                new GoThereState(EclipseMotionPlugState.INSTANCE),
                state(
                    leafCtrlBind('x', dontRepeat(new ChangeModeCommand(InsertExpandMode.NAME, InsertMode.RESUME_ON_MODE_ENTER))),
                    leafCtrlBind('n', dontRepeat(editText("hippieCompletion"))),
                    leafCtrlBind('p', dontRepeat(editText("hippieCompletion"))),
                    leafBind(new SimpleKeyStroke('c', EnumSet.of(Modifier.COMMAND)),
                            dontRepeat(cmd("org.eclipse.ui.edit.copy"))),
                    leafBind(new SimpleKeyStroke('x', EnumSet.of(Modifier.COMMAND)),
                           dontRepeat(cmd("org.eclipse.ui.edit.cut"))),
                    leafBind(new SimpleKeyStroke('v', EnumSet.of(Modifier.COMMAND)),
                            dontRepeat(cmd("org.eclipse.ui.edit.paste")))
                ));
    }
    
    @Override
    protected State<Command> contentAssistModeBindings() {
    	return state(
    		leafCtrlBind('n', dontRepeat(editText("goto.lineDown"))),
    		leafCtrlBind('p', dontRepeat(editText("goto.lineUp")))
        );
    }

}
