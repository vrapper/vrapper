package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SwapSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {

	@Override
	public void execute(EditorAdaptor editorAdaptor) {
		TextRange selection = editorAdaptor.getSelection();
		editorAdaptor.setPosition(selection.getEnd(), true);
		editorAdaptor.setSelection(new StartEndTextRange(selection.getEnd(), selection.getStart()));
	}

}
