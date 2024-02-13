package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;

import net.sourceforge.vrapper.eclipse.commands.EclipseMotionPlugState;
import net.sourceforge.vrapper.eclipse.commands.EclipseTextObjectPlugState;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.TextObjectState;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class EclipseTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider {

    @Override
    public State<DelimitedText> delimitedTexts() {
        return EmptyState.getInstance();
    }

    @Override
    public State<TextObject> textObjects() {
        return union(
                new TextObjectState(EclipseMotionPlugState.INSTANCE),
                EclipseTextObjectPlugState.INSTANCE);
    }

    @Override
    public String getName() {
        return "vrapper-eclipse-textobjects";
    }
}
