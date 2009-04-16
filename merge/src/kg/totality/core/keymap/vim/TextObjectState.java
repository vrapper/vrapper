package kg.totality.core.keymap.vim;

import kg.totality.core.commands.MotionTextObject;
import kg.totality.core.commands.TextObject;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.keymap.ConvertingState;
import kg.totality.core.keymap.State;
import kg.totality.core.utils.Function;

class Move2TextObject implements Function<TextObject, Motion> {
	@Override
	public TextObject call(Motion arg) {
		return new MotionTextObject(arg);
	}
}

public class TextObjectState extends ConvertingState<TextObject, Motion>  {

	public TextObjectState(State<Motion> wrapped) {
		super(new Move2TextObject(), wrapped);
	}

}
