package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.KeyStrokeConvertingState;
import net.sourceforge.vrapper.utils.Function;

/**
 * State providing a DelimiterHolder instance for any random key (creating an ad hoc
 * DelimiterHolder, hence the name).<br/> 
 * Soon to be implemented in Vim 7.4 as 'im&lt;char&gt;' and 'am&lt;char&gt;'.
 */
// Extend KeyStrokeConvertingState to make more clear what we're doing.
// A custom state would also work.
public class MatchAdHocDelimiterHolderState extends KeyStrokeConvertingState<DelimiterHolder> {
    
    public static final State<DelimiterHolder> INSTANCE = new MatchAdHocDelimiterHolderState();

    public MatchAdHocDelimiterHolderState() {
        super(new Converter());
    }

    static class Converter implements Function<DelimiterHolder, KeyStroke> {

        @Override
        public DelimiterHolder call(KeyStroke arg) {
            return new SimpleDelimiterHolder(Character.toString(arg.getCharacter()),
                    Character.toString(arg.getCharacter()));
        }
    }
}
