package net.sourceforge.vrapper.plugin.methodtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.methodtextobj.commands.MethodTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class MethodTextObjectProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new MethodTextObjectProvider();

    public MethodTextObjectProvider() {
        name = "methodtextobj State Provider";
    }

    @Override
    public State<TextObject> textObjects() {
        final TextObject innerArgument = MethodTextObject.INNER;
        final TextObject outerArgument = MethodTextObject.OUTER;

        @SuppressWarnings("unchecked")
        final State<TextObject> argObjects = state(
        		transitionBind('i',
        				state(leafBind('m', innerArgument))),
        		transitionBind('a',
        				state(leafBind('m', outerArgument))));
        return argObjects;
    }

}
