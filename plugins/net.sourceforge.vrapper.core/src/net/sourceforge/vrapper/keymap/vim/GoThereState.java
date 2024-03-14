package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class GoThereState extends ConvertingState<Command, Motion> {

    private static final MakeExecutableMotion converter = new MakeExecutableMotion();

    static class MakeExecutableMotion implements net.sourceforge.vrapper.utils.Function<Command, Motion> {
        public Command call(Motion motion) {
            return new MotionCommand(motion);
        }
    }

    public GoThereState(State<Motion> wrapped) {
        super(converter, wrapped);
    }

    public static Command motion2command(Motion motion) {
        return converter.call(motion);
    }
}
