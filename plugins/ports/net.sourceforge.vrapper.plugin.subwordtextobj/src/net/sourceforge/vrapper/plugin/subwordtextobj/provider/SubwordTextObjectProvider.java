package net.sourceforge.vrapper.plugin.subwordtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.subwordtextobj.commands.SubwordMotion;
import net.sourceforge.vrapper.plugin.subwordtextobj.commands.SubwordMotion.SubwordTextObject;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class SubwordTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider {

    @Override
    public String getName() {
        return "Subword TextObject Provider";
    }

    public State<TextObject> textObjects() {
        final State<TextObject> argObjects = state(
                leafBind(new PlugKeyStroke("(subword-inner)"), SubwordTextObject.INSTANCE),
                leafBind(new PlugKeyStroke("(subword-outer)"), SubwordTextObject.INSTANCE_OUTER),

                leafBind(new PlugKeyStroke("(subword-back)"), (TextObject)new MotionTextObject(SubwordMotion.SUB_BACK)),
                leafBind(new PlugKeyStroke("(subword-end)"), (TextObject)new MotionTextObject(SubwordMotion.SUB_END)),
                leafBind(new PlugKeyStroke("(subword-word)"), (TextObject)new MotionTextObject(SubwordMotion.SUB_WORD)));
        return argObjects;
    }
}
