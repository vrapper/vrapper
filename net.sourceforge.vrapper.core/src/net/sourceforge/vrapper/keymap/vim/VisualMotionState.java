package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LinewiseVisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionState extends ConvertingState<Command, Motion> {

    public static enum Motion2VMC implements Function<Command, Motion> {
        CHARWISE {
            public Command call(Motion arg) {
                return new VisualMotionCommand(arg);
            }
        },
        LINEWISE {
            public Command call(Motion arg) {
                return new LinewiseVisualMotionCommand(arg);
            }
        }
    }

    public VisualMotionState(Motion2VMC converter, State<Motion> wrapped) {
        super(converter, wrapped);
    }

}
