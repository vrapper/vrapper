package net.sourceforge.vrapper.plugin.subwordtextobj.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.plugin.subwordtextobj.commands.SubwordMotion;
import net.sourceforge.vrapper.vim.commands.Command;

public class SubwordProvider extends AbstractEclipseSpecificStateProvider {

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {
        return new GoThereState(state(
                leafBind(new PlugKeyStroke("(subword-back)"), SubwordMotion.SUB_BACK),
                leafBind(new PlugKeyStroke("(subword-end)"), SubwordMotion.SUB_END),
                leafBind(new PlugKeyStroke("(subword-word)"), SubwordMotion.SUB_WORD)));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> visualModeBindings() {
        return new VisualMotionState( state(
                leafBind(new PlugKeyStroke("(subword-back)"), SubwordMotion.SUB_BACK),
                leafBind(new PlugKeyStroke("(subword-end)"), SubwordMotion.SUB_END),
                leafBind(new PlugKeyStroke("(subword-word)"), SubwordMotion.SUB_WORD)));
    }

}
