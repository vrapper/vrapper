/**
 *
 */
package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;

public class ChangeModeCommand extends CountIgnoringNonRepeatableCommand {
	private final String modeName;

	public ChangeModeCommand(String modeName) {
		this.modeName = modeName;
	}

	@Override public void execute(EditorAdaptor editorMode) {
		editorMode.changeMode(modeName);
	}

}