package net.sourceforge.vrapper.plugin.surround.state;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.keymap.DynamicState;

public class DelimiterValues {

    @SuppressWarnings("unchecked")
    public static DynamicState<DelimiterHolder> createDelimiterState() {
        return new DynamicState<DelimiterHolder>(
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
                leafBind('`', (DelimiterHolder) new SimpleDelimiterHolder("`","`")),
                transitionBind('m', MatchAdHocDelimiterHolderState.INSTANCE));
    }
}
