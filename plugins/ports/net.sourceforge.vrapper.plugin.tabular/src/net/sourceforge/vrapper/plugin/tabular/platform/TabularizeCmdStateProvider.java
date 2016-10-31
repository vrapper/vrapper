package net.sourceforge.vrapper.plugin.tabular.platform;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.plugin.tabular.evaluator.AddPatternCmdEvaluator;
import net.sourceforge.vrapper.plugin.tabular.evaluator.TabularizeEvaluator;

public class TabularizeCmdStateProvider extends AbstractEclipseSpecificStateProvider {
	
	public TabularizeCmdStateProvider() {
		name = "TabularStateProvider";
		commands.add("Tabularize", TabularizeEvaluator.getInstance());
		commands.add("Tab", TabularizeEvaluator.getInstance());
		commands.add("AddTabularPattern", new AddPatternCmdEvaluator());
	}

}
