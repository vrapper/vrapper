package net.sourceforge.vrapper.plugin.indenttextobj.platform;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.indenttextobj.commands.IndentTextObject;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class IndentTextObjectProvider extends
        AbstractPlatformSpecificTextObjectProvider {

    @Override
    public String getName() {
        return "IndentTextObject";
    }

    @Override
    public State<DelimitedText> delimitedTexts() {
        return EmptyState.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public State<TextObject> textObjects() {
        return state(
        transitionBind('i',
            state(
                leafBind('i', IndentTextObject.INNER_INNER),
                leafBind('I', IndentTextObject.INNER_INNER))),
        transitionBind('a',
            state(
                leafBind('i', IndentTextObject.OUTER_INNER),
                leafBind('I', IndentTextObject.OUTER_OUTER)))
        );
    }

}
