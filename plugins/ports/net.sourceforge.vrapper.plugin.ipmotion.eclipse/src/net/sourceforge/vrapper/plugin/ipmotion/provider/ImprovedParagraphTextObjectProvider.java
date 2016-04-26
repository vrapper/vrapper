package net.sourceforge.vrapper.plugin.ipmotion.provider;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.TextObjectState;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.ipmotion.commands.motions.ImprovedParagraphMotion;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class ImprovedParagraphTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider  {
    
    @Override
    public String getName() {
        return "ipmotion State Provider";
    }

    @Override
    public State<TextObject> textObjects() {
        return new TextObjectState(ImprovedParagraphMotion.PARAGRAPH_MOTIONS);
    }

}
