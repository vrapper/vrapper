package net.sourceforge.vrapper.plugin.splitEditor.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.splitEditor.commands.MoveEditorCommand;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SplitEditorCommand;
import net.sourceforge.vrapper.plugin.splitEditor.commands.SwitchEditorCommand;
import net.sourceforge.vrapper.vim.commands.Command;

public class WindowCmdStateProvider extends AbstractEclipseSpecificStateProvider {

    public WindowCmdStateProvider() {
        commands.add("vsplit", SplitEditorCommand.VSPLIT);
        commands.add("split", SplitEditorCommand.HSPLIT);
        commands.add("mvsplit", SplitEditorCommand.VSPLIT_MOVE);
        commands.add("msplit", SplitEditorCommand.HSPLIT_MOVE);
        commands.add("wincmd_h", SwitchEditorCommand.SWITCH_LEFT);
        commands.add("wincmd_l", SwitchEditorCommand.SWITCH_RIGHT);
        commands.add("wincmd_k", SwitchEditorCommand.SWITCH_UP);
        commands.add("wincmd_j", SwitchEditorCommand.SWITCH_DOWN);
        commands.add("wincmd_H", MoveEditorCommand.MOVE_LEFT);
        commands.add("wincmd_L", MoveEditorCommand.MOVE_RIGHT);
        commands.add("wincmd_K", MoveEditorCommand.MOVE_UP);
        commands.add("wincmd_J", MoveEditorCommand.MOVE_DOWN);
        commands.add("wincmd_Hc", MoveEditorCommand.CLONE_LEFT);
        commands.add("wincmd_Lc", MoveEditorCommand.CLONE_RIGHT);
        commands.add("wincmd_Kc", MoveEditorCommand.CLONE_UP);
        commands.add("wincmd_Jc", MoveEditorCommand.CLONE_DOWN);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        Command switchEditorLeft = SwitchEditorCommand.SWITCH_LEFT;
        Command switchEditorRight = SwitchEditorCommand.SWITCH_RIGHT;
        Command switchEditorDown = SwitchEditorCommand.SWITCH_DOWN;
        Command switchEditorUp = SwitchEditorCommand.SWITCH_UP;

        Command cloneEditorLeft = MoveEditorCommand.CLONE_LEFT;
        Command cloneEditorRight = MoveEditorCommand.CLONE_RIGHT;
        Command cloneEditorDown = MoveEditorCommand.CLONE_DOWN;
        Command cloneEditorUp = MoveEditorCommand.CLONE_UP;
        return state(transitionBind(
                ctrlKey('w'),
                state(leafBind('h', switchEditorLeft),
                        leafBind('l', switchEditorRight),
                        leafBind('j', switchEditorDown),
                        leafBind('k', switchEditorUp),
                        leafBind(SpecialKey.ARROW_RIGHT, switchEditorRight),
                        leafBind(SpecialKey.ARROW_LEFT, switchEditorLeft),
                        leafBind(SpecialKey.ARROW_DOWN, switchEditorDown),
                        leafBind(SpecialKey.ARROW_UP, switchEditorUp),
                        leafBind('H', MoveEditorCommand.MOVE_LEFT),
                        leafBind('L', MoveEditorCommand.MOVE_RIGHT),
                        leafBind('J', MoveEditorCommand.MOVE_DOWN),
                        leafBind('K', MoveEditorCommand.MOVE_UP),
                        transitionBind(
                                'c',
                                leafBind('h', cloneEditorLeft),
                                leafBind('l', cloneEditorRight),
                                leafBind('j', cloneEditorDown),
                                leafBind('k', cloneEditorUp),
                                leafBind(SpecialKey.ARROW_RIGHT,
                                        cloneEditorRight),
                                leafBind(SpecialKey.ARROW_LEFT, cloneEditorLeft),
                                leafBind(SpecialKey.ARROW_DOWN, cloneEditorDown),
                                leafBind(SpecialKey.ARROW_UP, cloneEditorUp)))));
    }
}
