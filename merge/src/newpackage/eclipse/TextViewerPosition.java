package newpackage.eclipse;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;

import de.jroene.vrapper.vim.Space;
import newpackage.position.Position;

public class TextViewerPosition implements Position {

	private final ITextViewerExtension5 converter;
	private final Space space;
	private final int offset;

	public TextViewerPosition(ITextViewer textViewer, Space space, int offset) {
		this.converter = OffsetConverter.create(textViewer);
		this.space = space;
		this.offset = offset;
	}

	public TextViewerPosition(ITextViewerExtension5 converter, Space space, int offset) {
		this.converter = converter;
		this.space = space;
		this.offset = offset;
	}

	@Override
	public Position addModelOffset(int delta) {
		return new TextViewerPosition(converter, Space.MODEL, getModelOffset() + delta);
	}

	@Override
	public Position addViewOffset(int delta) {
		return new TextViewerPosition(converter, Space.MODEL, getModelOffset() + delta);
	}

	@Override
	public int getModelOffset() {
		switch (space) {
		case MODEL: return offset;
		case VIEW:  return converter.widgetOffset2ModelOffset(offset);
		default:
			throw new RuntimeException("bad space: " + space);
		}
	}

	@Override
	public int getViewOffset() {
		switch (space) {
		case VIEW:  return offset;
		case MODEL: return converter.modelOffset2WidgetOffset(offset);
		default:
			throw new RuntimeException("bad space: " + space);
		}
	}

}
