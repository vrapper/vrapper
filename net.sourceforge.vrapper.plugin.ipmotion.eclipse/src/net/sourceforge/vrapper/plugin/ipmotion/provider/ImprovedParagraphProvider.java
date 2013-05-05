package net.sourceforge.vrapper.plugin.ipmotion.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.ipmotion.commands.motions.ImprovedParagraphMotion;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class ImprovedParagraphProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new ImprovedParagraphProvider();
    
    public ImprovedParagraphProvider() {
        name = "ipmotion State Provider";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        
        final Motion paragraphBackward = ImprovedParagraphMotion.BACKWARD;
        final Motion paragraphForward = ImprovedParagraphMotion.FORWARD;
        
        final State<Motion> ipMotions = state(
                leafBind('{', paragraphBackward),
                leafBind('}', paragraphForward));
//        State<TextObject> ipObjects = new TextObjectState(ipMotions);
//        ipObjects = CountingState.wrap(ipObjects);
        return new GoThereState(ipMotions);
    }
}
