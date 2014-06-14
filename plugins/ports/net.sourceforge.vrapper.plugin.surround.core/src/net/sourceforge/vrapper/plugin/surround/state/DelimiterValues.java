package net.sourceforge.vrapper.plugin.surround.state;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

public class DelimiterValues {

    @SuppressWarnings("unchecked")
    public static DelimiterState createDelimiterState() {
        return new DelimiterState(
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
