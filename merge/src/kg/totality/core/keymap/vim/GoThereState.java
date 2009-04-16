package kg.totality.core.keymap.vim;

import kg.totality.core.commands.Command;
import kg.totality.core.commands.MotionCommand;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.keymap.ConvertingState;
import kg.totality.core.keymap.State;
import kg.totality.core.utils.Function;

public class GoThereState extends ConvertingState<Command, Motion> {

	private static final MakeExecutableMotion converter = new MakeExecutableMotion();

	static class MakeExecutableMotion implements Function<Command, Motion> {
		@Override
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
