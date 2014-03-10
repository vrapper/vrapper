package net.sourceforge.vrapper.plugin.splitEditor.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import java.util.Queue;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.splitEditor.commands.MoveEditorCommand;
import net.sourceforge.vrapper.plugin.splitEditor.commands.RemoveOtherWindowsCommand;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SplitContainer;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SplitDirection;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SplitEditorCommand;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SplitMode;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SwitchEditorCommand;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SwitchOtherEditorCommand;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public class WindowCmdStateProvider extends AbstractEclipseSpecificStateProvider {

    /**
     * :wincmd command parser.
     */
    class WinCmdCommandEvaluator implements Evaluator {
        @Override
        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            if (command.size() < 1 || command.peek().length() < 1) {
                vim.getUserInterfaceService().setErrorMessage(":wincmd expects a single argument");
            }
            final boolean bang = command.peek().equals("!");
            if (bang) {
                command.poll();
            }
            Command cmd = null;
            final String arg = command.poll();
            switch (arg.charAt(0)) {
            case 'h': cmd = SwitchEditorCommand.SWITCH_LEFT; break;
            case 'l': cmd = SwitchEditorCommand.SWITCH_RIGHT; break;
            case 'k': cmd = SwitchEditorCommand.SWITCH_UP; break;
            case 'j': cmd = SwitchEditorCommand.SWITCH_DOWN; break;
            case 'H': cmd = bang ? MoveEditorCommand.CLONE_LEFT  : MoveEditorCommand.MOVE_LEFT; break;
            case 'L': cmd = bang ? MoveEditorCommand.CLONE_RIGHT : MoveEditorCommand.MOVE_RIGHT; break;
            case 'K': cmd = bang ? MoveEditorCommand.CLONE_UP    : MoveEditorCommand.MOVE_UP; break;
            case 'J': cmd = bang ? MoveEditorCommand.CLONE_DOWN  : MoveEditorCommand.MOVE_DOWN; break;
            case 'o': cmd = bang ? RemoveOtherWindowsCommand.REMOVE_CLOSE  : RemoveOtherWindowsCommand.REMOVE_JOIN; break;
            case 'w': cmd = SwitchOtherEditorCommand.INSTANCE; break;
            }
            if (cmd != null) {
                try {
                    cmd.execute(vim);
                } catch (CommandExecutionException e) {
                    VrapperLog.error(":wincmd error", e);
                    vim.getUserInterfaceService().setErrorMessage(":wincmd error: " + e.getMessage());
                }
            } else {
                vim.getUserInterfaceService().setErrorMessage(":wincmd invalid argument '" + arg + "'");
            }
            return null;
        }
    }

    /**
     * :[v]split command evaluator.
     */
    class SplitEvaluator implements Evaluator {
        final SplitDirection direction;

        SplitEvaluator(SplitDirection direction)
        {
            this.direction = direction;
        }

        @Override
        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            final boolean bang = !command.isEmpty() && command.peek().equals("!");
            SplitMode mode = SplitMode.CLONE;
            if (bang) {
                mode = SplitMode.MOVE;
                command.poll();
            }
            SplitContainer containerMode = SplitContainer.SHARED_AREA;
            if (!command.isEmpty() && command.peek().startsWith("++nos")) {
                containerMode = SplitContainer.TOP_LEVEL;
                command.poll();
            }
            String filename = null;
            if(!command.isEmpty()) {
            	//use join in case this is a Windows path with spaces in it
            	filename = StringUtils.join(" ", command.toArray());
            }

            try {
                new SplitEditorCommand(direction, mode, containerMode, filename).execute(vim);
            } catch (CommandExecutionException e) {
                VrapperLog.error(":[v]split error", e);
                vim.getUserInterfaceService().setErrorMessage(":[v]split error: " + e.getMessage());
            }
            return null;
        }
    }

    public WindowCmdStateProvider() {
        name = "EditSplit State Provider";
        commands.add("vsplit", new SplitEvaluator(SplitDirection.VERTICALLY));
        commands.add("split", new SplitEvaluator(SplitDirection.HORIZONTALLY));
        commands.add("wincmd", new WinCmdCommandEvaluator());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        final Command vsplit = SplitEditorCommand.VSPLIT;
        final Command split = SplitEditorCommand.HSPLIT;
        final Command removeOther = RemoveOtherWindowsCommand.REMOVE_JOIN;
        final Command switchOther = SwitchOtherEditorCommand.INSTANCE;

        final Command switchEditorLeft = SwitchEditorCommand.SWITCH_LEFT;
        final Command switchEditorRight = SwitchEditorCommand.SWITCH_RIGHT;
        final Command switchEditorDown = SwitchEditorCommand.SWITCH_DOWN;
        final Command switchEditorUp = SwitchEditorCommand.SWITCH_UP;

        final Command cloneEditorLeft = MoveEditorCommand.CLONE_LEFT;
        final Command cloneEditorRight = MoveEditorCommand.CLONE_RIGHT;
        final Command cloneEditorDown = MoveEditorCommand.CLONE_DOWN;
        final Command cloneEditorUp = MoveEditorCommand.CLONE_UP;

        return state(transitionBind(
                ctrlKey('w'),
                  state(
                        leafBind('s', split),
                        leafBind('S', split),
                        leafBind(ctrlKey('s'), split),
                        leafBind('v', vsplit),
                        leafBind(ctrlKey('v'), vsplit),
                        leafBind('h', switchEditorLeft),
                        leafBind('l', switchEditorRight),
                        leafBind('j', switchEditorDown),
                        leafBind('k', switchEditorUp),
                        leafBind(SpecialKey.ARROW_RIGHT, switchEditorRight),
                        leafBind(SpecialKey.ARROW_LEFT, switchEditorLeft),
                        leafBind(SpecialKey.ARROW_DOWN, switchEditorDown),
                        leafBind(SpecialKey.ARROW_UP, switchEditorUp),
                        leafBind(ctrlKey('h'), switchEditorLeft),
                        leafBind(ctrlKey('l'), switchEditorRight),
                        leafBind(ctrlKey('j'), switchEditorDown),
                        leafBind(ctrlKey('k'), switchEditorUp),
                        leafBind('H', MoveEditorCommand.MOVE_LEFT),
                        leafBind('L', MoveEditorCommand.MOVE_RIGHT),
                        leafBind('J', MoveEditorCommand.MOVE_DOWN),
                        leafBind('K', MoveEditorCommand.MOVE_UP),
                        leafBind('w', switchOther),
                        leafBind(ctrlKey('w'), switchOther),
                        leafBind('o', removeOther),
                        leafBind(ctrlKey('o'), removeOther),
                        transitionBind(
                                'c',
                                leafBind('h', cloneEditorLeft),
                                leafBind('l', cloneEditorRight),
                                leafBind('j', cloneEditorDown),
                                leafBind('k', cloneEditorUp),
                                leafBind(SpecialKey.ARROW_RIGHT, cloneEditorRight),
                                leafBind(SpecialKey.ARROW_LEFT, cloneEditorLeft),
                                leafBind(SpecialKey.ARROW_DOWN, cloneEditorDown),
                                leafBind(SpecialKey.ARROW_UP, cloneEditorUp)))));
    }
}
