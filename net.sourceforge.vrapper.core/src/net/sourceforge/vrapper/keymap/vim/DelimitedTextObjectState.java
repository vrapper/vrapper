package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.InnerTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class DelimitedTextObjectState extends ConvertingState<TextObject, DelimitedText> {
    public static final boolean INNER = false;
    public static final boolean OUTER = true;
    
    private static Function<TextObject, DelimitedText> outerConverter = new Function<TextObject, DelimitedText>() {
        public TextObject call(DelimitedText delimitedText) {
            return new OuterTextObject(delimitedText);
        }
    };
    private static Function<TextObject, DelimitedText> innerConverter = new Function<TextObject, DelimitedText>() {
        public TextObject call(DelimitedText delimitedText) {
            return new InnerTextObject(delimitedText);
        }
    };
    
    public DelimitedTextObjectState(State<DelimitedText> wrapped, boolean includeDelimiter) {
        super(includeDelimiter ? outerConverter : innerConverter, wrapped);
    }

}
