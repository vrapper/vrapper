package net.sourceforge.vrapper.plugin.argtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.argtextobj.commands.ArgumentTextObject;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class ArgumentTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider {
    
    private String name;

    public ArgumentTextObjectProvider() {
        name = "argtextobj State Provider";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State<DelimitedText> delimitedTexts() {
        return EmptyState.getInstance();
    }

    @Override
    public State<TextObject> textObjects() {
        final TextObject innerArgument = ArgumentTextObject.INNER;
        final TextObject outerArgument = ArgumentTextObject.OUTER;

        @SuppressWarnings("unchecked")
        final State<TextObject> argObjects = state(
                            transitionBind('i',
                                    state(leafBind('a', innerArgument))),
                            transitionBind('a',
                                    state(leafBind('a', outerArgument))));
        return argObjects;
    }
}
