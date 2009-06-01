package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

/**
 * Wrapper command for motions in visual mode.
 *
 * @author Matthias Radig
 */
public class LinewiseVisualMotionCommand extends AbstractVisualMotionCommand {

    public LinewiseVisualMotionCommand(Motion motion) {
        super(motion);
    }

    @Override
    protected void extendSelection(EditorAdaptor editorAdaptor,
            Selection oldSelection) {
		Position newSelectionEnd = editorAdaptor.getPosition();
		Position newSelectionStart;
		if (oldSelection.isReversed()) {
            newSelectionStart = oldSelection.getStart().addModelOffset(-1);
        } else {
            newSelectionStart = oldSelection.getStart();
        }
		editorAdaptor.setSelection(createLinewiseSelection(editorAdaptor, newSelectionStart, newSelectionEnd));
    }

    @Override
    protected Selection getSelection(EditorAdaptor editorAdaptor) {
		Selection oldSelection = editorAdaptor.getSelection();
		if (oldSelection == null) {
			Position position = editorAdaptor.getPosition();
			oldSelection = createLinewiseSelection(editorAdaptor, position, position);
			editorAdaptor.setSelection(oldSelection);
		}
		return oldSelection;
    }

    private Selection createLinewiseSelection(EditorAdaptor editorAdaptor,
            Position start, Position end) {
        LineInformation sLine = editorAdaptor.getViewContent().getLineInformationOfOffset(start.getViewOffset());
        LineInformation eLine = editorAdaptor.getViewContent().getLineInformationOfOffset(end.getViewOffset());
        if (sLine.getNumber() < eLine.getNumber()) {
            return new Selection(new StartEndTextRange(
                    start.setViewOffset(sLine.getBeginOffset()),
                    end.setViewOffset(eLine.getEndOffset()+1)),
                    ContentType.LINES);
        } else {
            return new Selection(new StartEndTextRange(
                    end.setViewOffset(sLine.getEndOffset()+1),
                    start.setViewOffset(eLine.getBeginOffset())),
                    ContentType.LINES);
        }
    }

}
