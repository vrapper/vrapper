package net.sourceforge.vrapper.plugin.linetextobj.platform;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.linetextobj.commands.LineTextObject;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class LineTextObjectProvider extends
        AbstractPlatformSpecificTextObjectProvider {

    @Override
    public String getName() {
        return "LineTextObject";
    }

    @Override
    public State<DelimitedText> delimitedTexts() {
        return EmptyState.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public State<TextObject> textObjects() {
        return state(
                transitionBind('i', leafBind('l', LineTextObject.INNER)),
                transitionBind('a', leafBind('l', LineTextObject.OUTER)));
    }

}
