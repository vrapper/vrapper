package net.sourceforge.vrapper.plugin.surround.state;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyBinding;

public class DelimiterState extends HashMapState<DelimiterHolder> {

    // Extensible state to be used to dynamically add new surrounds.
    public DelimiterState(KeyBinding<DelimiterHolder>... bindings) {
        super(asList(bindings));
    }

    public void addDelimiterHolder(char key, String left, String right)
    {
        KeyBinding<DelimiterHolder> binding =
                leafBind(key, (DelimiterHolder) new SimpleDelimiterHolder(left, right));
        map.put(binding.getKeyPress(), binding.getTransition());
    }

}