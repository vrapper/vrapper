package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
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
    protected Selection getSelection(EditorAdaptor editorAdaptor) {
		Selection oldSelection = editorAdaptor.getSelection();
		if (oldSelection == null) {
			oldSelection = new SimpleSelection(
			        new StartEndTextRange(
			                editorAdaptor.getPosition(),
			                editorAdaptor.getPosition()));
			editorAdaptor.setSelection(oldSelection);
		}
		else {
			oldSelection = checkForWindowsNewlines(oldSelection, editorAdaptor);
		}
		return oldSelection;
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
	
	/**
	 * Windows newlines (\r\n) are causing problems.  If a selection starts or ends
	 * with only one of those two characters, Eclipse throws an exception.
	 */
	private Selection checkForWindowsNewlines(Selection selection, EditorAdaptor editorAdaptor) {
		TextContent content = editorAdaptor.getModelContent();
		Position start = selection.getLeftBound();
		Position end = selection.getRightBound();
		int startOffset = start.getModelOffset();
		int endOffset = end.getModelOffset();
		String text = content.getText(selection);
		//are we stopping half-way between a multi-byte newline?
		//(\r\n on Windows, Eclipse can't handle separating them)
		if(text.startsWith("\n")) {
			if(startOffset > 0 && content.getText(startOffset-1, 1).equals("\r")) {
				//include preceding \r
				start = start.addModelOffset(-1);
			}
		}
		if(text.endsWith("\r")) {
			if(endOffset < content.getTextLength() && content.getText(endOffset, 1).equals("\n")) {
				//don't include \r
				end = end.addModelOffset(-1);
			}
		}
		
		if(selection.isReversed()) {
			return new SimpleSelection(selection.getFrom(), selection.getTo(),
				new StartEndTextRange(end, start));
		}
		else {
			return new SimpleSelection(selection.getFrom(), selection.getTo(),
				new StartEndTextRange(start, end));
		}
	}
}
