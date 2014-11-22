package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
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

	    String selectionMode = editorAdaptor.getConfiguration().get(Options.SELECTION);
	    if (Selection.EXCLUSIVE.equals(selectionMode)) {
	        if (motionBorderPolicy == BorderPolicy.INCLUSIVE && selGrowsToRight) {
	            newTo = VimUtils.safeAddModelOffset(editorAdaptor, newTo, 1, true);
	        }
	        newSelection = new SimpleSelection(from, newTo, new StartEndTextRange(from, newTo));
	    } else if (Selection.INCLUSIVE.equals(selectionMode)) {
	        CursorService cursorService = editorAdaptor.getCursorService();
	        int docLen = editorAdaptor.getModelContent().getTextLength();
	        // to must be "on" the last character of the file, not behind it.
	        // The case for an empty file is handled in shiftPositionForModelOffset()
	        if (newTo.getModelOffset() == docLen) {
	            newTo = cursorService.shiftPositionForModelOffset(newTo.getModelOffset(), -1, true);
	        }
	        newSelection = new SimpleSelection(from, newTo,
	                StartEndTextRange.inclusive(cursorService, from, newTo));
	    } else {
	        VrapperLog.error("Unhandled 'selection' value " + selectionMode);
	        // Shouldn't happen?
	        newSelection = null;
	    }
	    editorAdaptor.setSelection(newSelection);
	}
}
