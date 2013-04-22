package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
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
		else {
			oldSelection = checkForWindowsNewlines(oldSelection, editorAdaptor);
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
	    
	    Selection newSelection;
	    if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive")) {
            newSelection = new SimpleSelection(
	                new StartEndTextRange(oldSelection.getStart(), newSelectionEnd));
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
	        newSelection = new SimpleSelection(
	                new StartEndTextRange(newSelectionStart, newSelectionEnd));
	    }
        newSelection = checkForWindowsNewlines(newSelection, editorAdaptor);
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
			return new SimpleSelection(
				new StartEndTextRange(end, start));
		}
		else {
			return new SimpleSelection(
				new StartEndTextRange(start, end));
		}
	}
}
