package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionState extends ConvertingState<Command, Motion> {

    private static class ConvertMotion implements Function<Command, Motion> {
        public static final ConvertMotion INSTANCE = new ConvertMotion();
        @Override
        public Command call(final Motion arg) {
            return new VisualMotionCommand(arg);
        }
    }

    public VisualMotionState(final State<Motion> wrapped) {
        super(ConvertMotion.INSTANCE, wrapped);
    }

}
