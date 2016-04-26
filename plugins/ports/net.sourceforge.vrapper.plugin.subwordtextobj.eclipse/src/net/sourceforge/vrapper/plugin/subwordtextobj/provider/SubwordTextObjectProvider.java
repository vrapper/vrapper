package net.sourceforge.vrapper.plugin.subwordtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.subwordtextobj.commands.SubwordMotion.SubwordTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class SubwordTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider {

    @Override
    public String getName() {
        return "Subword TextObject Provider";
    }

    public State<TextObject> textObjects() {
        @SuppressWarnings("unchecked")
        final State<TextObject> argObjects = state(
                transitionBind('i',
                    state( leafBind('\\', SubwordTextObject.INSTANCE))),
                transitionBind('a',
                    state( leafBind('\\', SubwordTextObject.INSTANCE_OUTER))));
        return argObjects;
    }
}
