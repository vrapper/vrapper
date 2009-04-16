package newpackage.eclipse;

import kg.totality.core.ui.UIUtils;
import kg.totality.core.utils.CaretType;
import newpackage.glue.CursorService;
import newpackage.glue.SelectionService;
import newpackage.position.Position;
import newpackage.position.StartEndTextRange;
import newpackage.position.TextRange;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;

import de.jroene.vrapper.eclipse.VrapperPlugin;
import de.jroene.vrapper.vim.Space;

public class EclipseCursorAndSelection implements CursorService, SelectionService {

	private final ITextViewer textViewer;
	private int stickyColumn;
	private boolean stickToEOL = false;
	private ITextViewerExtension5 converter;

	public EclipseCursorAndSelection(ITextViewer textViewer) {
		this.textViewer = textViewer;
		converter = OffsetConverter.create(textViewer);
	}

	@Override
	public Position getPosition() {
		return new TextViewerPosition(textViewer, Space.VIEW, textViewer.getTextWidget().getCaretOffset());
	}

	@Override
	public void setPosition(Position position, boolean updateColumn) {
		textViewer.getTextWidget().setCaretOffset(position.getViewOffset());
		if (updateColumn) {
			stickToEOL = false;
			stickyColumn = textViewer.getTextWidget().getLocationAtOffset(position.getViewOffset()).x;
		}
	}

	@Override
	public Position stickyColumnAtViewLine(int lineNo) {
		StyledText tw = textViewer.getTextWidget();
		if (!stickToEOL) {
			try {
				int y = tw.getLocationAtOffset(tw.getOffsetAtLine(lineNo)).y;
				int offset = tw.getOffsetAtLocation(new Point(stickyColumn, y));
				return new TextViewerPosition(textViewer, Space.VIEW, offset);
			} catch (IllegalArgumentException e) {
				// fall through silently and return line end
			}
		}
		int lineLen = tw.getLine(lineNo).length();
		int offset = tw.getOffsetAtLine(lineNo) + lineLen;
		return new TextViewerPosition(textViewer, Space.VIEW, offset);
	}

	@Override
	public TextRange getSelection() {
		int start, end, pos, len;
		start = end = textViewer.getSelectedRange().x;
		len = textViewer.getSelectedRange().y;
		pos = textViewer.getTextWidget().getCaretOffset();
		if (start == pos)
			start += len;
		else
			end += len;


		Position from = new TextViewerPosition(textViewer, Space.MODEL, start);
		Position to =   new TextViewerPosition(textViewer, Space.MODEL, end);
		return new StartEndTextRange(from, to);
	}

	@Override
	public void setSelection(TextRange selection) {
		if (selection == null) {
			int cursorPos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
			textViewer.setSelectedRange(cursorPos, 0);
		} else {
			textViewer.getTextWidget().setCaretOffset(selection.getStart().getViewOffset());
			int from = selection.getStart().getModelOffset();
			int length = !selection.isReversed() ? selection.getModelLength() : -selection.getModelLength();
			textViewer.setSelectedRange(from, length);
		}
	}

	@Override
	public void setLineWiseSelection(boolean lineWise) {
		VrapperPlugin.error("line wise selection not implemented");
	}

	@Override
	public Position newPositionForModelOffset(int offset) {
		return new TextViewerPosition(textViewer, Space.MODEL, offset);
	}

	@Override
	public Position newPositionForViewOffset(int offset) {
		return new TextViewerPosition(textViewer, Space.VIEW, offset);
	}

	@Override
	public void setCaret(CaretType caretType) {
		StyledText styledText = textViewer.getTextWidget();
		styledText.setCaret(UIUtils.createCaret(caretType, styledText));
	}

	@Override
	public void stickToEOL() {
		stickToEOL = true;
	}

}
