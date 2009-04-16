package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class DotCommand extends CountAwareCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		Command lastCommand = editorAdaptor.getRegisterManager().getLastEdit();
		if (lastCommand != null) {
			if (count != NO_COUNT_GIVEN)
				lastCommand = lastCommand.withCount(count);
			lastCommand.execute(editorAdaptor);
		}
	}

	@Override
	public CountAwareCommand repetition() {
		return null;
	}

}
