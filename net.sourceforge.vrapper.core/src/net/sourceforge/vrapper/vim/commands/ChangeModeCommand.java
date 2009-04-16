/**
 *
 */
package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ChangeModeCommand extends CountIgnoringNonRepeatableCommand {
	private final String modeName;

	public ChangeModeCommand(String modeName) {
		this.modeName = modeName;
	}

	@Override public void execute(EditorAdaptor editorMode) {
		editorMode.changeMode(modeName);
	}

}