package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.BlockwiseVisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LinewiseVisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionState extends ConvertingState<Command, Motion> {

    public static enum Motion2VMC implements Function<Command, Motion> {
        CHARWISE {
            @Override
            public Command call(final Motion arg) {
                return new VisualMotionCommand(arg);
            }
        },
        LINEWISE {
            @Override
            public Command call(final Motion arg) {
                return new LinewiseVisualMotionCommand(arg);
            }
        },
        BLOCKWISE {
            @Override
            public Command call(final Motion arg) {
                return new BlockwiseVisualMotionCommand(arg);
            }
        }
    }

    public VisualMotionState(final Motion2VMC converter, final State<Motion> wrapped) {
        super(converter, wrapped);
    }

}
