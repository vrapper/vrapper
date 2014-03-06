package net.sourceforge.vrapper.plugin.ipmotion.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.TextObjectState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.ipmotion.commands.motions.ImprovedParagraphMotion;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class ImprovedParagraphProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new ImprovedParagraphProvider();
    
    public ImprovedParagraphProvider() {
        name = "ipmotion State Provider";
    }
    
    @Override
    protected State<TextObject> textObjects() {
        return new TextObjectState(getMotions());
    }

    @Override
    protected State<Command> normalModeBindings(State<TextObject> textObjects) {
        return new GoThereState(getMotions());
    }

    private State<Motion> getMotions() {
        final Motion paragraphBackward = ImprovedParagraphMotion.BACKWARD;
        final Motion paragraphForward = ImprovedParagraphMotion.FORWARD;

        @SuppressWarnings("unchecked")
        final State<Motion> ipMotions = state(
                leafBind('{', paragraphBackward),
                leafBind('}', paragraphForward));
        return ipMotions;
    }

    @Override
    protected State<Command> visualModeBindings() {
//        final State<Command> motionCommands = new GoThereState(ipMotions);
        return new VisualMotionState(Motion2VMC.CHARWISE, getMotions());
    }
}
