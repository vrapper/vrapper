package net.sourceforge.vrapper.plugin.sneak.provider;


import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.plugin.sneak.modes.ChangeToSneakModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;

public class SneakStateProvider extends AbstractEclipseSpecificStateProvider {

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {

        Command sneakInput = ChangeToSneakModeCommand.withoutHints();
        Command sneakInputBackwards = ChangeToSneakModeCommand.backwards();

        Command sneakNext = new MotionCommand(ContinueFindingMotion.NORMAL_NAVIGATING);
        Command sneakPrev = new MotionCommand(ContinueFindingMotion.REVERSE_NAVIGATING);

        return state(
            leafBind(new PlugKeyStroke("(sneak_s)"), sneakInput),
            leafBind(new PlugKeyStroke("(sneak_S)"), sneakInputBackwards),
            leafBind(new PlugKeyStroke("(sneak-next)"), sneakNext),
            leafBind(new PlugKeyStroke("(sneak-prev)"), sneakPrev)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> visualModeBindings() {

        Command sneakInput = ChangeToSneakModeCommand.fromVisual();
        Command sneakInputBackwards = ChangeToSneakModeCommand.backwardsAndFromVisual();

        Command sneakNext = new VisualMotionCommand(ContinueFindingMotion.NORMAL_NAVIGATING);
        Command sneakPrev = new VisualMotionCommand(ContinueFindingMotion.REVERSE_NAVIGATING);

        return state(
            leafBind(new PlugKeyStroke("(sneak_s)"), sneakInput),
            leafBind(new PlugKeyStroke("(sneak_S)"), sneakInputBackwards),
            leafBind(new PlugKeyStroke("(sneak-next)"), sneakNext),
            leafBind(new PlugKeyStroke("(sneak-prev)"), sneakPrev)
        );
    }
}
