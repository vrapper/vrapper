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
import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveUpDownNonWhitespace;

public class SubwordProvider extends AbstractEclipseSpecificStateProvider {

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {
        //since I'm overriding the default ',' and '_' behavior
        //add ',,' and '__' to access those default features
        return new GoThereState(state(
                transitionBind('_',
                        state(
                                leafBind('b', SubwordMotion.SNAKE_BACK),
                                leafBind('e', SubwordMotion.SNAKE_END),
                                leafBind('w', SubwordMotion.SNAKE_WORD),
                                leafBind('_', (Motion)MoveUpDownNonWhitespace.MOVE_DOWN_LESS_ONE))),
                transitionBind(',',
                        state(
                                leafBind('b', SubwordMotion.CAMEL_BACK),
                                leafBind('e', SubwordMotion.CAMEL_END),
                                leafBind('w', SubwordMotion.CAMEL_WORD),
                                leafBind(',', (Motion)ContinueFindingMotion.REVERSE)))
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> visualModeBindings() {
        return new VisualMotionState(
                state(
                transitionBind('_',
                        state(
                                leafBind('b', SubwordMotion.SNAKE_BACK),
                                leafBind('e', SubwordMotion.SNAKE_END),
                                leafBind('w', SubwordMotion.SNAKE_WORD),
                                leafBind('_', (Motion)MoveUpDownNonWhitespace.MOVE_DOWN_LESS_ONE))),
                transitionBind(',',
                        state(
                                leafBind('b', SubwordMotion.CAMEL_BACK),
                                leafBind('e', SubwordMotion.CAMEL_END),
                                leafBind('w', SubwordMotion.CAMEL_WORD),
                                leafBind(',', (Motion)ContinueFindingMotion.REVERSE)))
                ));
    }

}
