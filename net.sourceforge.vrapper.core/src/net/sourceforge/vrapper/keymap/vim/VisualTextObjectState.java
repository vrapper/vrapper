package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.SelectTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class VisualTextObjectState extends ConvertingState<Command, TextObject> {
    
    public static final Function<Command, TextObject> converter = new Function<Command, TextObject>() {
        public Command call(TextObject arg) {
            return new SelectTextObjectCommand(arg);
        }
    };
    
    public static final State<Command> INSTANCE = new VisualTextObjectState();
    
    public VisualTextObjectState() {
        super(converter, NormalMode.textObjects());
    }

}
