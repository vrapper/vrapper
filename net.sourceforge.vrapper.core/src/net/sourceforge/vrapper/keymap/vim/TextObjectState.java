package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

class Move2TextObject implements Function<TextObject, Motion> {
    public TextObject call(Motion arg) {
        return new MotionTextObject(arg);
    }
}

public class TextObjectState extends ConvertingState<TextObject, Motion>  {

    public TextObjectState(State<Motion> wrapped) {
        super(new Move2TextObject(), wrapped);
    }

}
