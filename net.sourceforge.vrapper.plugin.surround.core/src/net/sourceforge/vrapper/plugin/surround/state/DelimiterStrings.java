package net.sourceforge.vrapper.plugin.surround.state;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;

public class DelimiterStrings {

    @SuppressWarnings("unchecked")
    public static final State<String> DELIMITER_STRINGS = state(
            leafBind('b', "(\0)"),
            leafBind('(', "( \0 )"),
            leafBind(')', "(\0)"),
            leafBind('[', "[ \0 ]"),
            leafBind(']', "[\0]"),
            leafBind('B', "{\0}"),
            leafBind('{', "{ \0 }"),
            leafBind('}', "{\0}"),
            leafBind('a', "<\0>"),
            leafBind('<', "< \0 >"),
            leafBind('>', "<\0>"),
            leafBind('\'', "'\0'"),
            leafBind('"', "\"\0\""),
            leafBind('`', "`\0`"));
    
}
