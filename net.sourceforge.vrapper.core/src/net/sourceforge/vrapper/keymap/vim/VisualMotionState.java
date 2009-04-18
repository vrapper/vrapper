package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionState extends ConvertingState<Command, Motion> {

    private static class Motion2VMC implements Function<Command, Motion> {
        public Command call(Motion arg) {
            return new VisualMotionCommand(arg);
        }
    }

    public VisualMotionState(State<Motion> wrapped) {
        super(new Motion2VMC(), wrapped);
    }

}
