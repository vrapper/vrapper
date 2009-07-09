package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionCommand extends AbstractVisualMotionCommand {

	public VisualMotionCommand(Motion motion) {
		super(motion);
	}

	@Override
    protected Selection getSelection(EditorAdaptor editorAdaptor) {
		Selection oldSelection = editorAdaptor.getSelection();
		if (oldSelection == null) {
			oldSelection = new SimpleSelection(
			        new StartEndTextRange(
			                editorAdaptor.getPosition(),
			                editorAdaptor.getPosition()));
			editorAdaptor.setSelection(oldSelection);
		}
		return oldSelection;
	}

	@Override
    protected void extendSelection(EditorAdaptor editorAdaptor, Selection oldSelection) {
		Position newSelectionEnd = editorAdaptor.getPosition();
		// TODO: behaves saner than Vim when shrinking; option?
		if (motion.borderPolicy() == BorderPolicy.INCLUSIVE) {
            newSelectionEnd = newSelectionEnd.addViewOffset(1);
        }
		editorAdaptor.setSelection(new SimpleSelection(
		        new StartEndTextRange(oldSelection.getStart(), newSelectionEnd)));
	}
}
