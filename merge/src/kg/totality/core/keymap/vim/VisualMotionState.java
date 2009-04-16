package kg.totality.core.keymap.vim;

import kg.totality.core.commands.Command;
import kg.totality.core.commands.VisualMotionCommand;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.keymap.ConvertingState;
import kg.totality.core.keymap.State;
import kg.totality.core.utils.Function;

public class VisualMotionState extends ConvertingState<Command, Motion> {

	private static class Motion2VMC implements Function<Command, Motion> {
		@Override public Command call(Motion arg) {
		return new VisualMotionCommand(arg);
		}
	}

	public VisualMotionState(State<Motion> wrapped) {
		super(new Motion2VMC(), wrapped);
	}

}
