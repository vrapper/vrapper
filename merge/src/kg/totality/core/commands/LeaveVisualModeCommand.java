/**
 *
 */
package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.modes.NormalMode;

public class LeaveVisualModeCommand extends CountIgnoringNonRepeatableCommand {
	@Override public void execute(EditorAdaptor editorAdaptor) {
		// FIXME: compatibility option: don't set caret offset
		editorAdaptor.setSelection(null);
		editorAdaptor.changeMode(NormalMode.NAME);
	}
}