package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.QuoteDelimitedText;

/**
 * State providing a DelimitedText instance for any random key (creating an ad hoc
 * DelimitedText, hence the name).<br/> 
 * Soon to be implemented in Vim 7.4 as 'im&lt;char&gt;' and 'am&lt;char&gt;'.
 */
// Extend KeyStrokeConvertingState to make more clear what we're doing.
// A custom state would also work.
public class MatchAdHocDelimitedTextState extends KeyStrokeConvertingState<DelimitedText> {
    
    public static final State<DelimitedText> INSTANCE = new MatchAdHocDelimitedTextState();

    public MatchAdHocDelimitedTextState() {
        super(new Converter());
    }

    static class Converter implements Function<DelimitedText, KeyStroke> {

        @Override
        public DelimitedText call(KeyStroke arg) {
            return new QuoteDelimitedText(arg.getCharacter());
        }
    }
}
