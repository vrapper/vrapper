package net.sourceforge.vrapper.plugin.surround.state;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import java.util.Collections;

import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyBinding;
import net.sourceforge.vrapper.keymap.State;

public class DelimiterValues {

    public static class DelimiterState extends HashMapState<DelimiterHolder> {

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

    @SuppressWarnings("unchecked")
    public static DelimiterState DELIMITER_REGISTRY = new DelimiterState(
            leafBind('b', (DelimiterHolder) new SimpleDelimiterHolder("(",")")),
            leafBind('(', (DelimiterHolder) new SimpleDelimiterHolder("( "," )")),
            leafBind(')', (DelimiterHolder) new SimpleDelimiterHolder("(",")")),
            leafBind('[', (DelimiterHolder) new SimpleDelimiterHolder("[ "," ]")),
            leafBind(']', (DelimiterHolder) new SimpleDelimiterHolder("[","]")),
            leafBind('B', (DelimiterHolder) new SimpleDelimiterHolder("{","}")),
            leafBind('{', (DelimiterHolder) new SimpleDelimiterHolder("{ "," }")),
            leafBind('}', (DelimiterHolder) new SimpleDelimiterHolder("{","}")),
            leafBind('a', (DelimiterHolder) new SimpleDelimiterHolder("<",">")),
            leafBind('<', (DelimiterHolder) new XMLTagDynamicDelimiterHolder()),
            leafBind('t', (DelimiterHolder) new XMLTagDynamicDelimiterHolder()),
            leafBind('>', (DelimiterHolder) new SimpleDelimiterHolder("<",">")),
            leafBind('\'', (DelimiterHolder) new SimpleDelimiterHolder("'","'")),
            leafBind('"', (DelimiterHolder) new SimpleDelimiterHolder("\"","\"")),
            leafBind('`', (DelimiterHolder) new SimpleDelimiterHolder("`","`")));

    public static State<DelimiterHolder> DELIMITER_HOLDER_STATE =
                DELIMITER_REGISTRY
                .union(
                    new HashMapState<DelimiterHolder>(
                            Collections.singletonList(
                                    transitionBind('m', MatchAdHocDelimiterHolderState.INSTANCE))));
}
