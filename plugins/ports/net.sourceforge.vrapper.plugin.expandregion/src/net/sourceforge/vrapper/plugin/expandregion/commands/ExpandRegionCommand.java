package net.sourceforge.vrapper.plugin.expandregion.commands;

import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ExpandRegionCommand extends EclipseCommand {

	private static final String EXPAND_NAME = "org.eclipse.jdt.ui.edit.text.java.select.enclosing";
	private static final String SHRINK_NAME = "org.eclipse.jdt.ui.edit.text.java.select.last";

	public static ExpandRegionCommand EXPAND = new ExpandRegionCommand(
			EXPAND_NAME);

	public static ExpandRegionCommand SHRINK = new ExpandRegionCommand(
			SHRINK_NAME);

	public ExpandRegionCommand(String action) {
		super(action);
	}

	public void execute(EditorAdaptor editorAdaptor) {
		doIt(1, getCommandName(), editorAdaptor, true);
	}
}