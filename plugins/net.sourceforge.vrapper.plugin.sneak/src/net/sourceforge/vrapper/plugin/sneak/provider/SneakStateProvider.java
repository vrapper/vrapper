package net.sourceforge.vrapper.plugin.sneak.provider;


import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.plugin.sneak.modes.ChangeToSneakModeCommand;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakInputMode.CharOffsetHint;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakInputMode.InputCharsLimitHint;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;

public class SneakStateProvider extends AbstractEclipseSpecificStateProvider {

    @Override
    protected State<Command> normalModeBindings() {

        Command sneakInput = ChangeToSneakModeCommand.withoutHints();
        Command sneakInputBackwards = ChangeToSneakModeCommand.backwards();

        Command sneak_f = ChangeToSneakModeCommand.withHints(CharOffsetHint.F_CHARS, InputCharsLimitHint.ONE);
        Command sneak_F = ChangeToSneakModeCommand.backwards(CharOffsetHint.F_CHARS, InputCharsLimitHint.ONE);

        Command sneak_t = ChangeToSneakModeCommand.withHints(CharOffsetHint.T_CHAR_FORWARD, InputCharsLimitHint.ONE);
        Command sneak_T = ChangeToSneakModeCommand.backwards(CharOffsetHint.T_CHAR_BACKWARD, InputCharsLimitHint.ONE);
        
        Command sneakNext = new MotionCommand(ContinueFindingMotion.NORMAL_NAVIGATING);
        Command sneakPrev = new MotionCommand(ContinueFindingMotion.REVERSE_NAVIGATING);

        return state(
            leafBind(new PlugKeyStroke("(sneak_s)"), sneakInput),
            leafBind(new PlugKeyStroke("(sneak_S)"), sneakInputBackwards),
            leafBind(new PlugKeyStroke("(sneak_f)"), sneak_f),
            leafBind(new PlugKeyStroke("(sneak_F)"), sneak_F),
            leafBind(new PlugKeyStroke("(sneak_t)"), sneak_t),
            leafBind(new PlugKeyStroke("(sneak_T)"), sneak_T),
            leafBind(new PlugKeyStroke("(sneak-next)"), sneakNext),
            leafBind(new PlugKeyStroke("(sneak-prev)"), sneakPrev)
        );
    }

    @Override
    protected State<Command> visualModeBindings() {

        Command sneakInput = ChangeToSneakModeCommand.fromVisual();
        Command sneakInputBackwards = ChangeToSneakModeCommand.backwardsAndFromVisual();

        Command sneak_f = ChangeToSneakModeCommand.fromVisual(CharOffsetHint.F_CHARS, InputCharsLimitHint.ONE);
        Command sneak_F = ChangeToSneakModeCommand.backwardsAndFromVisual(CharOffsetHint.F_CHARS, InputCharsLimitHint.ONE);

        Command sneak_t = ChangeToSneakModeCommand.fromVisual(CharOffsetHint.T_CHAR_FORWARD, InputCharsLimitHint.ONE);
        Command sneak_T = ChangeToSneakModeCommand.backwardsAndFromVisual(CharOffsetHint.T_CHAR_BACKWARD, InputCharsLimitHint.ONE);
        
        Command sneakNext = new VisualMotionCommand(ContinueFindingMotion.NORMAL_NAVIGATING);
        Command sneakPrev = new VisualMotionCommand(ContinueFindingMotion.REVERSE_NAVIGATING);

        return state(
            leafBind(new PlugKeyStroke("(sneak_s)"), sneakInput),
            leafBind(new PlugKeyStroke("(sneak_S)"), sneakInputBackwards),
            leafBind(new PlugKeyStroke("(sneak_f)"), sneak_f),
            leafBind(new PlugKeyStroke("(sneak_F)"), sneak_F),
            leafBind(new PlugKeyStroke("(sneak_t)"), sneak_t),
            leafBind(new PlugKeyStroke("(sneak_T)"), sneak_T),
            leafBind(new PlugKeyStroke("(sneak-next)"), sneakNext),
            leafBind(new PlugKeyStroke("(sneak-prev)"), sneakPrev)
        );
    }
}
