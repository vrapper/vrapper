package net.sourceforge.vrapper.plugin.subwordtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.plugin.subwordtextobj.commands.SubwordMotion;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveUpDownNonWhitespace;

public class SubwordProvider extends AbstractEclipseSpecificStateProvider {

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {
        //since I'm overriding the default '_' behavior
        //add '__' to access that default feature
        return new GoThereState(state(
                transitionBind('_',
                        state(
                                leafBind('b', SubwordMotion.SUB_BACK),
                                leafBind('e', SubwordMotion.SUB_END),
                                leafBind('w', SubwordMotion.SUB_WORD),
                                leafBind('_', (Motion)MoveUpDownNonWhitespace.MOVE_DOWN_LESS_ONE)))
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> visualModeBindings() {
        return new VisualMotionState(
                state(
                transitionBind('_',
                        state(
                                leafBind('b', SubwordMotion.SUB_BACK),
                                leafBind('e', SubwordMotion.SUB_END),
                                leafBind('w', SubwordMotion.SUB_WORD),
                                leafBind('_', (Motion)MoveUpDownNonWhitespace.MOVE_DOWN_LESS_ONE)))
                ));
    }

}
