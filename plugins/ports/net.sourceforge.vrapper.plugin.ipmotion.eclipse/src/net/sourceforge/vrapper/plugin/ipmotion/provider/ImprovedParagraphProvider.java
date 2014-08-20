package net.sourceforge.vrapper.plugin.ipmotion.provider;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.ipmotion.commands.motions.ImprovedParagraphMotion;
import net.sourceforge.vrapper.vim.commands.Command;

public class ImprovedParagraphProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new ImprovedParagraphProvider();
    
    public ImprovedParagraphProvider() {
        name = "ipmotion State Provider";
    }
    
    @Override
    protected State<Command> normalModeBindings() {
        return new GoThereState(ImprovedParagraphMotion.PARAGRAPH_MOTIONS);
    }
    
    @Override
    protected State<Command> visualModeBindings() {
        return new VisualMotionState(Motion2VMC.LINEWISE, ImprovedParagraphMotion.PARAGRAPH_MOTIONS);
    }
}
