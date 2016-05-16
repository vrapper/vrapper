package net.sourceforge.vrapper.plugin.cycle.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.cycle.commands.CycleCommand;
import net.sourceforge.vrapper.vim.commands.Command;

public class CycleProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new CycleProvider();
    
    public CycleProvider() {
        name = "cycle State Provider";
        commands.add("AddCycleGroup", CycleCommand.addCycleGroupEvaluator());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {
        return state(
                leafCtrlBind('a', CycleCommand.NEXT),
                leafCtrlBind('x', CycleCommand.PREV));
    }
}
