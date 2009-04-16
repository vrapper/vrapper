package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionCommand extends MotionCommand {

	public VisualMotionCommand(Motion motion) {
		super(motion);
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		TextRange oldSelection = getSelection(editorAdaptor);
		super.execute(editorAdaptor, count);
		extendSelection(editorAdaptor, oldSelection);
	}

	private TextRange getSelection(EditorAdaptor editorAdaptor) {
		TextRange oldSelection = editorAdaptor.getSelection();
		if (oldSelection == null) {
			oldSelection = new StartEndTextRange(editorAdaptor.getPosition(), editorAdaptor.getPosition());
			editorAdaptor.setSelection(oldSelection);
		}
		return oldSelection;
	}

	private void extendSelection(EditorAdaptor editorAdaptor, TextRange oldSelection) {
		Position newSelectionEnd = editorAdaptor.getPosition();
		// TODO: behaves saner than Vim when shrinking; option?
		if (motion.borderPolicy() == BorderPolicy.INCLUSIVE)
			newSelectionEnd = newSelectionEnd.addViewOffset(1);
		editorAdaptor.setSelection(new StartEndTextRange(oldSelection.getStart(), newSelectionEnd));
	}

}
