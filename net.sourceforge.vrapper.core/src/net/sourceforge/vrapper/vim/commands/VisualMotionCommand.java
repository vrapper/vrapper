package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class VisualMotionCommand extends AbstractVisualMotionCommand {

	public VisualMotionCommand(Motion motion) {
		super(motion);
	}

	@Override
	protected void extendSelection(EditorAdaptor editorAdaptor, Selection oldSelection,
	        int motionCount) {
	    Position from = oldSelection.getFrom();
	    Position newTo = editorAdaptor.getPosition();
	    Position oldTo = oldSelection.getTo();
	    boolean selReversed = from.compareTo(newTo) > 0;
	    boolean selShiftsToRight = newTo.compareTo(oldTo) > 0;
	    boolean selGrowsToRight = selShiftsToRight && ! selReversed;

	    BorderPolicy motionBorderPolicy = getMotion(motionCount).borderPolicy();
	    Selection newSelection;

	    if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals(Selection.EXCLUSIVE)) {
	        if (motionBorderPolicy == BorderPolicy.INCLUSIVE && selGrowsToRight) {
	            newTo = VimUtils.safeAddModelOffset(editorAdaptor, newTo, 1, true);
	        }
	        newSelection = new SimpleSelection(from, newTo, new StartEndTextRange(from, newTo));
	    } else {
	        newSelection = new SimpleSelection(from, newTo,
	                StartEndTextRange.inclusive(editorAdaptor.getCursorService(), from, newTo));
	    }
	    editorAdaptor.setSelection(newSelection);
	}
}
