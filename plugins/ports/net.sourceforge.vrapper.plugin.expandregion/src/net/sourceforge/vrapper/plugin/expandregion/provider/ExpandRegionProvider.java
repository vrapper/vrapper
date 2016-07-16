package net.sourceforge.vrapper.plugin.expandregion.provider;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.plugin.expandregion.commands.ExpandRegionCommand;
import net.sourceforge.vrapper.vim.commands.Command;

public class ExpandRegionProvider extends AbstractEclipseSpecificStateProvider {

	@Override
	public String getName() {
		return "ExpandRegion Provider";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected State<Command> visualModeBindings() {
		Command expand = ExpandRegionCommand.EXPAND;
		Command shrink = ExpandRegionCommand.SHRINK;

		return state(leafBind('+', expand), leafBind('_', shrink));
	}

}
