package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
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
	    Position newSelectionStart = oldSelection.getStart();
	    Position oldSelectionEnd = oldSelection.getEnd();
	    Position newSelectionEnd = editorAdaptor.getPosition();
	    // TODO: behaves saner than Vim when shrinking; option?
	    if (motion.borderPolicy() == BorderPolicy.INCLUSIVE)
            newSelectionEnd = newSelectionEnd.addViewOffset(1);
	    
	    if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive")) {
            editorAdaptor.setSelection(new SimpleSelection(
	                new StartEndTextRange(oldSelection.getStart(), newSelectionEnd)));
        } else {
            if (newSelectionStart.getModelOffset()-newSelectionEnd.getModelOffset() == 1)
                newSelectionEnd = newSelectionEnd.addModelOffset(1);
	        // always keep the character at selection start selected
	        int oldCmp = newSelectionStart.compareTo(oldSelectionEnd);
	        int newCmp = newSelectionStart.compareTo(newSelectionEnd);
	        if (newCmp == 0 || oldCmp != newCmp) {
	            newSelectionStart = newSelectionStart.addModelOffset(-oldCmp);
	            if (newCmp == 0 && oldCmp == -1) {
	                newSelectionEnd = newSelectionEnd.addModelOffset(-1);
	            }
	            newCmp = newSelectionStart.compareTo(newSelectionEnd);
	            CaretType type = newCmp == -1 ? CaretType.LEFT_SHIFTED_RECTANGULAR: CaretType.RECTANGULAR;
	            editorAdaptor.getCursorService().setCaret(type);
	        }
	        editorAdaptor.setSelection(new SimpleSelection(
	                new StartEndTextRange(newSelectionStart, newSelectionEnd)));
	    }
	}
}
