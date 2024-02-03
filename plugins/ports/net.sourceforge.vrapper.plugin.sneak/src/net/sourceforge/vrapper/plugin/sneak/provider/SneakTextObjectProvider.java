package net.sourceforge.vrapper.plugin.sneak.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.plugin.sneak.keymap.SneakFindCharactersTextObjectState;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class SneakTextObjectProvider extends AbstractPlatformSpecificTextObjectProvider {

    @Override
    public String getName() {
        return SneakTextObjectProvider.class.getName();
    }

    @Override
    public State<TextObject> textObjects() {
        // [TODO] Implement sneaks of custom length
        // [TODO] Hook up ; and ,
        // Currently these will invoke vrapper's default behavior which means the last fX or dfX
        // motion gets used rather than whatever gets called by (sneak-next)
        return state(
                transitionBind(new PlugKeyStroke("(sneak_s)"), SneakFindCharactersTextObjectState.forwards(2)),
                transitionBind(new PlugKeyStroke("(sneak_S)"), SneakFindCharactersTextObjectState.backwards(2))
            );
    }
}
