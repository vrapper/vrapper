package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.Motion;
import newpackage.position.StartEndTextRange;
import newpackage.position.Position;
import newpackage.position.TextRange;

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
