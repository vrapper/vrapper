package net.sourceforge.vrapper.plugin.methodtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.methodtextobj.commands.MethodTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class MethodTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider {

    @Override
    public String getName() {
        return "Method TextObject Provider";
    }

    public State<TextObject> textObjects() {
        final TextObject innerArgument = MethodTextObject.INNER;
        final TextObject outerArgument = MethodTextObject.OUTER;

        @SuppressWarnings("unchecked")
        final State<TextObject> argObjects = state(
                transitionBind('i',
                        state(leafBind('f', innerArgument))),
                transitionBind('a',
                        state(leafBind('f', outerArgument))));
        return argObjects;
    }
}
