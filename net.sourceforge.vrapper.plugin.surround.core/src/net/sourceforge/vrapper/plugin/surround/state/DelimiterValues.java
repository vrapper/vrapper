package net.sourceforge.vrapper.plugin.surround.state;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;

public class DelimiterValues {

    @SuppressWarnings("unchecked")
    public static final State<DelimiterHolder> DELIMITER_HOLDERS = state(
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
    
}
